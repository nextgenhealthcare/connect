package com.webreach.mirth.plugins.extensionmanager;

import com.thoughtworks.xstream.annotations.XStreamAlias;
@XStreamAlias("extensionInfo")
public class ExtensionInfo
{
    private String id;
    private String name;
    private String version;
    private String mirthVersion;
    private String downloadUrl;
    private String author;
    private String description;
    private String url;
    private String type;
    
    public String getDownloadUrl()
    {
        return downloadUrl;
    }
    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }
    public String getMirthVersion()
    {
        return mirthVersion;
    }
    public void setMirthVersion(String mirthVersion)
    {
        this.mirthVersion = mirthVersion;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getVersion()
    {
        return version;
    }
    public void setVersion(String version)
    {
        this.version = version;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getAuthor()
    {
        return author;
    }
    public void setAuthor(String author)
    {
        this.author = author;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
}
