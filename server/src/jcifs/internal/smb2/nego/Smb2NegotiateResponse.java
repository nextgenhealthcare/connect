/*
 * © 2017 AgNO3 Gmbh & Co. KG
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jcifs.internal.smb2.nego;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.CIFSContext;
import jcifs.Configuration;
import jcifs.DialectVersion;
import jcifs.internal.CommonServerMessageBlock;
import jcifs.internal.SMBProtocolDecodingException;
import jcifs.internal.SmbNegotiationRequest;
import jcifs.internal.SmbNegotiationResponse;
import jcifs.internal.smb2.ServerMessageBlock2Response;
import jcifs.internal.smb2.Smb2Constants;
import jcifs.internal.smb2.io.Smb2ReadResponse;
import jcifs.internal.smb2.io.Smb2WriteRequest;
import jcifs.internal.util.SMBUtil;
import jcifs.util.Hexdump;
import jcifs.util.transport.Response;


/**
 * @author mbechler
 *
 */
public class Smb2NegotiateResponse extends ServerMessageBlock2Response implements SmbNegotiationResponse {

    private static final Logger log = LoggerFactory.getLogger(Smb2NegotiateResponse.class);

    private int securityMode;
    private int dialectRevision;
    private byte[] serverGuid = new byte[16];
    private int capabilities;
    private int commonCapabilities;
    private int maxTransactSize;
    private int maxReadSize;
    private int maxWriteSize;
    private long systemTime;
    private long serverStartTime;
    private NegotiateContextResponse[] negotiateContexts;
    private byte[] securityBuffer;
    private DialectVersion selectedDialect;

    private boolean supportsEncryption;
    private int selectedCipher = -1;
    private int selectedPreauthHash = -1;


    /**
     * 
     * @param cfg
     */
    public Smb2NegotiateResponse ( Configuration cfg ) {
        super(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#getInitialCredits()
     */
    @Override
    public int getInitialCredits () {
        return getCredit();
    }


    /**
     * @return the dialectRevision
     */
    public int getDialectRevision () {
        return this.dialectRevision;
    }


    /**
     * @return the serverGuid
     */
    public byte[] getServerGuid () {
        return this.serverGuid;
    }


    /**
     * @return the selectedDialect
     */
    @Override
    public DialectVersion getSelectedDialect () {
        return this.selectedDialect;
    }


    /**
     * @return the selectedCipher
     */
    public int getSelectedCipher () {
        return this.selectedCipher;
    }


    /**
     * @return the selectedPreauthHash
     */
    public int getSelectedPreauthHash () {
        return this.selectedPreauthHash;
    }


    /**
     * @return the server returned capabilities
     */
    public final int getCapabilities () {
        return this.capabilities;
    }


    /**
     * @return the common/negotiated capabilieis
     */
    public final int getCommonCapabilities () {
        return this.commonCapabilities;
    }


    /**
     * @return initial security blob
     */
    public byte[] getSecurityBlob () {
        return this.securityBuffer;
    }


    /**
     * @return the maxTransactSize
     */
    public int getMaxTransactSize () {
        return this.maxTransactSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#getTransactionBufferSize()
     */
    @Override
    public int getTransactionBufferSize () {
        return getMaxTransactSize();
    }


    /**
     * @return the negotiateContexts
     */
    public NegotiateContextResponse[] getNegotiateContexts () {
        return this.negotiateContexts;
    }


    /**
     * @return the serverStartTime
     */
    public long getServerStartTime () {
        return this.serverStartTime;
    }


    /**
     * @return the securityMode
     */
    public int getSecurityMode () {
        return this.securityMode;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#haveCapabilitiy(int)
     */
    @Override
    public boolean haveCapabilitiy ( int cap ) {
        return ( this.commonCapabilities & cap ) == cap;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#isDFSSupported()
     */
    @Override
    public boolean isDFSSupported () {
        return !getConfig().isDfsDisabled() && haveCapabilitiy(Smb2Constants.SMB2_GLOBAL_CAP_DFS);
    }


    /**
     * 
     * @return whether SMB encryption is supported by the server
     */
    public boolean isEncryptionSupported () {
        return this.supportsEncryption;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#canReuse(jcifs.CIFSContext, boolean)
     */
    @Override
    public boolean canReuse ( CIFSContext tc, boolean forceSigning ) {
        return getConfig().equals(tc.getConfig());
    }


    @Override
    public boolean isValid ( CIFSContext tc, SmbNegotiationRequest req ) {
        if ( !isReceived() || getStatus() != 0 ) {
            return false;
        }

        if ( req.isSigningEnforced() && !isSigningEnabled() ) {
            log.error("Signing is enforced but server does not allow it");
            return false;
        }

        if ( getDialectRevision() == Smb2Constants.SMB2_DIALECT_ANY ) {
            log.error("Server returned ANY dialect");
            return false;
        }

        Smb2NegotiateRequest r = (Smb2NegotiateRequest) req;

        DialectVersion selected = null;
        for ( DialectVersion dv : DialectVersion.values() ) {
            if ( !dv.isSMB2() ) {
                continue;
            }
            if ( dv.getDialect() == getDialectRevision() ) {
                selected = dv;
            }
        }

        if ( selected == null ) {
            log.error("Server returned an unknown dialect");
            return false;
        }

        if ( !selected.atLeast(getConfig().getMinimumVersion()) || !selected.atMost(getConfig().getMaximumVersion()) ) {
            log.error(
                String.format(
                    "Server selected an disallowed dialect version %s (min: %s max: %s)",
                    selected,
                    getConfig().getMinimumVersion(),
                    getConfig().getMaximumVersion()));
            return false;
        }
        this.selectedDialect = selected;

        // Filter out unsupported capabilities
        this.commonCapabilities = r.getCapabilities() & this.capabilities;

        if ( ( this.commonCapabilities & Smb2Constants.SMB2_GLOBAL_CAP_ENCRYPTION ) != 0 ) {
            this.supportsEncryption = tc.getConfig().isEncryptionEnabled();
        }

        if ( this.selectedDialect.atLeast(DialectVersion.SMB311) ) {
            if ( !checkNegotiateContexts(r, this.commonCapabilities) ) {
                return false;
            }
        }

        int maxBufferSize = tc.getConfig().getTransactionBufferSize();
        this.maxReadSize = Math.min(maxBufferSize - Smb2ReadResponse.OVERHEAD, Math.min(tc.getConfig().getReceiveBufferSize(), this.maxReadSize))
                & ~0x7;
        this.maxWriteSize = Math.min(maxBufferSize - Smb2WriteRequest.OVERHEAD, Math.min(tc.getConfig().getSendBufferSize(), this.maxWriteSize))
                & ~0x7;
        this.maxTransactSize = Math.min(maxBufferSize - 512, this.maxTransactSize) & ~0x7;

        return true;
    }


    private boolean checkNegotiateContexts ( Smb2NegotiateRequest req, int caps ) {
        if ( this.negotiateContexts == null || this.negotiateContexts.length == 0 ) {
            log.error("Response lacks negotiate contexts");
            return false;
        }

        boolean foundPreauth = false, foundEnc = false;
        for ( NegotiateContextResponse ncr : this.negotiateContexts ) {
            if ( ncr == null ) {
                continue;
            }
            else if ( !foundEnc && ncr.getContextType() == EncryptionNegotiateContext.NEGO_CTX_ENC_TYPE ) {
                foundEnc = true;
                EncryptionNegotiateContext enc = (EncryptionNegotiateContext) ncr;
                if ( !checkEncryptionContext(req, enc) ) {
                    return false;
                }
                this.selectedCipher = enc.getCiphers()[ 0 ];
                this.supportsEncryption = true;
            }
            else if ( ncr.getContextType() == EncryptionNegotiateContext.NEGO_CTX_ENC_TYPE ) {
                log.error("Multiple encryption negotiate contexts");
                return false;
            }
            else if ( !foundPreauth && ncr.getContextType() == PreauthIntegrityNegotiateContext.NEGO_CTX_PREAUTH_TYPE ) {
                foundPreauth = true;
                PreauthIntegrityNegotiateContext pi = (PreauthIntegrityNegotiateContext) ncr;
                if ( !checkPreauthContext(req, pi) ) {
                    return false;
                }
                this.selectedPreauthHash = pi.getHashAlgos()[ 0 ];
            }
            else if ( ncr.getContextType() == PreauthIntegrityNegotiateContext.NEGO_CTX_PREAUTH_TYPE ) {
                log.error("Multiple preauth negotiate contexts");
                return false;
            }
        }

        if ( !foundPreauth ) {
            log.error("Missing preauth negotiate context");
            return false;
        }
        if ( !foundEnc && ( caps & Smb2Constants.SMB2_GLOBAL_CAP_ENCRYPTION ) != 0 ) {
            log.error("Missing encryption negotiate context");
            return false;
        }
        else if ( !foundEnc ) {
            log.debug("No encryption support");
        }
        return true;
    }


    private static boolean checkPreauthContext ( Smb2NegotiateRequest req, PreauthIntegrityNegotiateContext pc ) {
        if ( pc.getHashAlgos() == null || pc.getHashAlgos().length != 1 ) {
            log.error("Server returned no hash selection");
            return false;
        }

        PreauthIntegrityNegotiateContext rpc = null;
        for ( NegotiateContextRequest rnc : req.getNegotiateContexts() ) {
            if ( rnc instanceof PreauthIntegrityNegotiateContext ) {
                rpc = (PreauthIntegrityNegotiateContext) rnc;
            }
        }
        if ( rpc == null ) {
            return false;
        }

        boolean valid = false;
        for ( int hash : rpc.getHashAlgos() ) {
            if ( hash == pc.getHashAlgos()[ 0 ] ) {
                valid = true;
            }
        }
        if ( !valid ) {
            log.error("Server returned invalid hash selection");
            return false;
        }
        return true;
    }


    private static boolean checkEncryptionContext ( Smb2NegotiateRequest req, EncryptionNegotiateContext ec ) {
        if ( ec.getCiphers() == null || ec.getCiphers().length != 1 ) {
            log.error("Server returned no cipher selection");
            return false;
        }

        EncryptionNegotiateContext rec = null;
        for ( NegotiateContextRequest rnc : req.getNegotiateContexts() ) {
            if ( rnc instanceof EncryptionNegotiateContext ) {
                rec = (EncryptionNegotiateContext) rnc;
            }
        }
        if ( rec == null ) {
            return false;
        }

        boolean valid = false;
        for ( int cipher : rec.getCiphers() ) {
            if ( cipher == ec.getCiphers()[ 0 ] ) {
                valid = true;
            }
        }
        if ( !valid ) {
            log.error("Server returned invalid cipher selection");
            return false;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#getReceiveBufferSize()
     */
    @Override
    public int getReceiveBufferSize () {
        return this.maxReadSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#getSendBufferSize()
     */
    @Override
    public int getSendBufferSize () {
        return this.maxWriteSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#isSigningEnabled()
     */
    @Override
    public boolean isSigningEnabled () {
        return ( this.securityMode & ( Smb2Constants.SMB2_NEGOTIATE_SIGNING_ENABLED ) ) != 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#isSigningRequired()
     */
    @Override
    public boolean isSigningRequired () {
        return ( this.securityMode & Smb2Constants.SMB2_NEGOTIATE_SIGNING_REQUIRED ) != 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#isSigningNegotiated()
     */
    @Override
    public boolean isSigningNegotiated () {
        return isSigningRequired();
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#setupRequest(jcifs.internal.CommonServerMessageBlock)
     */
    @Override
    public void setupRequest ( CommonServerMessageBlock request ) {}


    /**
     * 
     * {@inheritDoc}
     *
     * @see jcifs.internal.SmbNegotiationResponse#setupResponse(jcifs.util.transport.Response)
     */
    @Override
    public void setupResponse ( Response resp ) {}


    @Override
    protected int readBytesWireFormat ( byte[] buffer, int bufferIndex ) throws SMBProtocolDecodingException {
        int start = bufferIndex;

        int structureSize = SMBUtil.readInt2(buffer, bufferIndex);
        if ( structureSize != 65 ) {
            throw new SMBProtocolDecodingException("Structure size is not 65");
        }

        this.securityMode = SMBUtil.readInt2(buffer, bufferIndex + 2);
        bufferIndex += 4;

        this.dialectRevision = SMBUtil.readInt2(buffer, bufferIndex);
        int negotiateContextCount = SMBUtil.readInt2(buffer, bufferIndex + 2);
        bufferIndex += 4;

        System.arraycopy(buffer, bufferIndex, this.serverGuid, 0, 16);
        bufferIndex += 16;

        this.capabilities = SMBUtil.readInt4(buffer, bufferIndex);
        bufferIndex += 4;

        this.maxTransactSize = SMBUtil.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.maxReadSize = SMBUtil.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.maxWriteSize = SMBUtil.readInt4(buffer, bufferIndex);
        bufferIndex += 4;

        this.systemTime = SMBUtil.readTime(buffer, bufferIndex);
        bufferIndex += 8;
        this.serverStartTime = SMBUtil.readTime(buffer, bufferIndex);
        bufferIndex += 8;

        int securityBufferOffset = SMBUtil.readInt2(buffer, bufferIndex);
        int securityBufferLength = SMBUtil.readInt2(buffer, bufferIndex + 2);
        bufferIndex += 4;

        int negotiateContextOffset = SMBUtil.readInt4(buffer, bufferIndex);
        bufferIndex += 4;

        int hdrStart = getHeaderStart();
        if ( hdrStart + securityBufferOffset + securityBufferLength < buffer.length ) {
            this.securityBuffer = new byte[securityBufferLength];
            System.arraycopy(buffer, hdrStart + securityBufferOffset, this.securityBuffer, 0, securityBufferLength);
            bufferIndex += securityBufferLength;
        }

        int pad = ( bufferIndex - hdrStart ) % 8;
        bufferIndex += pad;

        // Modified the following based on commit and comments in https://github.com/AgNO3/jcifs-ng/issues/178
        int ncpos = getHeaderStart() + negotiateContextOffset;
        boolean ignoreNcpos = true;
        if ( this.dialectRevision == 0x0311 && negotiateContextOffset != 0 && negotiateContextCount != 0 ) {
            ignoreNcpos = false;
            NegotiateContextResponse[] contexts = new NegotiateContextResponse[negotiateContextCount];
            for ( int i = 0; i < negotiateContextCount; i++ ) {
                int type = SMBUtil.readInt2(buffer, ncpos);
                int dataLen = SMBUtil.readInt2(buffer, ncpos + 2);
                ncpos += 4;
                ncpos += 4; // Reserved
                NegotiateContextResponse ctx = createContext(type);
                if ( ctx != null ) {
                    ctx.decode(buffer, ncpos, dataLen);
                    contexts[ i ] = ctx;
                }
                ncpos += dataLen;
                if ( i != negotiateContextCount - 1 ) {
                    ncpos += pad8(ncpos);
                }
            }
            this.negotiateContexts = contexts;
        }

        return Math.max(bufferIndex, ignoreNcpos ? bufferIndex : ncpos) - start;
    }


    /**
     * @param type
     * @return
     */
    protected static NegotiateContextResponse createContext ( int type ) {
        switch ( type ) {
        case EncryptionNegotiateContext.NEGO_CTX_ENC_TYPE:
            return new EncryptionNegotiateContext();
        case PreauthIntegrityNegotiateContext.NEGO_CTX_PREAUTH_TYPE:
            return new PreauthIntegrityNegotiateContext();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.internal.smb2.ServerMessageBlock2#writeBytesWireFormat(byte[], int)
     */
    @Override
    protected int writeBytesWireFormat ( byte[] dst, int dstIndex ) {
        return 0;
    }


    @Override
    public String toString () {
        return new String(
            "Smb2NegotiateResponse[" + super.toString() + ",dialectRevision=" + this.dialectRevision + ",securityMode=0x"
                    + Hexdump.toHexString(this.securityMode, 1) + ",capabilities=0x" + Hexdump.toHexString(this.capabilities, 8) + ",serverTime="
                    + new Date(this.systemTime));
    }

}
