package org.mozilla.javascript.tools.debugger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Kit;

public class MirthDim extends Dim {

	private boolean stopping = false;

	public void setStopping(boolean stopping) {
		this.stopping = stopping;
	}

	@Override
	protected void handleBreakpointHit(StackFrame frame, Context cx) {
		if (!stopping) {
			super.handleBreakpointHit(frame, cx);
		}
	}

	@Override
	protected void interrupted(Context cx, final StackFrame frame, Throwable scriptException) {
		ContextData contextData = frame.contextData();
		boolean eventThreadFlag = callback.isGuiEventThread();
		contextData.eventThreadFlag = eventThreadFlag;

		boolean recursiveEventThreadCall = false;

	interruptedCheck: synchronized (eventThreadMonitor) {
			if (eventThreadFlag) {
				if (interruptedContextData != null) {
					recursiveEventThreadCall = true;
					break interruptedCheck;
				}
			} else {
				while (interruptedContextData != null) {
					try {
						eventThreadMonitor.wait();
					} catch (InterruptedException exc) {
						return;
					}
				}
			}
			interruptedContextData = contextData;
		}

		if (recursiveEventThreadCall) {
			// XXX: For now the following is commented out as on Linux
			// too deep recursion of dispatchNextGuiEvent causes GUI lockout.
			// Note: it can make GUI unresponsive if long-running script
			// will be called on GUI thread while processing another interrupt
			if (false) {
				// Run event dispatch until gui sets a flag to exit the initial
				// call to interrupted.
				while (this.returnValue == -1) {
					try {
						callback.dispatchNextGuiEvent();
					} catch (InterruptedException exc) {
					}
				}
			}
			return;
		}

		if (interruptedContextData == null)
			Kit.codeBug();

		try {
			do {
				int frameCount = contextData.frameCount();
				this.frameIndex = frameCount - 1;

				final String threadTitle = Thread.currentThread().toString();
				final String alertMessage;
				if (scriptException == null) {
					alertMessage = null;
				} else {
					alertMessage = scriptException.toString();
				}

				int returnValue = -1;
				if (!eventThreadFlag) {
					synchronized (monitor) {
						if (insideInterruptLoop)
							Kit.codeBug();
						this.insideInterruptLoop = true;
						this.evalRequest = null;
						this.returnValue = -1;
						callback.enterInterrupt(frame, threadTitle, alertMessage);
						try {
							for (;;) {
								if (stopping) {
									break;
								}
								try {
									monitor.wait();
								} catch (InterruptedException exc) {
									Thread.currentThread().interrupt();
									break;
								}
								if (evalRequest != null) {
									this.evalResult = null;
									try {
										evalResult = do_eval(cx, evalFrame, evalRequest);
									} finally {
										evalRequest = null;
										evalFrame = null;
										monitor.notify();
									}
									continue;
								}
								if (this.returnValue != -1) {
									returnValue = this.returnValue;
									break;
								}
							}
						} finally {
							insideInterruptLoop = false;
						}
					}
				} else {
					this.returnValue = -1;
					callback.enterInterrupt(frame, threadTitle, alertMessage);
					while (this.returnValue == -1) {
						try {
							callback.dispatchNextGuiEvent();
						} catch (InterruptedException exc) {
						}
					}
					returnValue = this.returnValue;
				}
				switch (returnValue) {
				case STEP_OVER:
					contextData.breakNextLine = true;
					contextData.stopAtFrameDepth = contextData.frameCount();
					break;
				case STEP_INTO:
					contextData.breakNextLine = true;
					contextData.stopAtFrameDepth = -1;
					break;
				case STEP_OUT:
					if (contextData.frameCount() > 1) {
						contextData.breakNextLine = true;
						contextData.stopAtFrameDepth = contextData.frameCount() - 1;
					}
					break;
				}
			} while (false);
		} finally {
			synchronized (eventThreadMonitor) {
				interruptedContextData = null;
				eventThreadMonitor.notifyAll();
			}
		}

	}
}
