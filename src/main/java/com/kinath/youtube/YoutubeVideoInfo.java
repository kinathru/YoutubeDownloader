package com.kinath.youtube;

import org.jsoup.nodes.Document;

public class YoutubeVideoInfo
{
    private String videoTitle;
    private String videoDescription;
    private Document htmlDoc;

    public String getVideoTitle()
    {
        return videoTitle;
    }

    public void setVideoTitle( String videoTitle )
    {
        this.videoTitle = videoTitle;
    }

    public String getVideoDescription()
    {
        return videoDescription;
    }

    public void setVideoDescription( String videoDescription )
    {
        this.videoDescription = videoDescription;
    }

    public Document getHtmlDoc()
    {
        return htmlDoc;
    }

    public void setHtmlDoc( Document htmlDoc )
    {
        this.htmlDoc = htmlDoc;
    }
}
