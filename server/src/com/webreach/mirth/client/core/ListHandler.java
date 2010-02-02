package com.webreach.mirth.client.core;

import java.util.List;

public interface ListHandler {
    public List<?> getFirstPage() throws ListHandlerException;

    public List<?> getNextPage() throws ListHandlerException;

    public List<?> getPreviousPage() throws ListHandlerException;

    public int getSize() throws ListHandlerException;

    public void resetIndex();
}
