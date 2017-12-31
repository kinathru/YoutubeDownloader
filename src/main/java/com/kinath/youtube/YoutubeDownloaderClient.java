package com.kinath.youtube;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class YoutubeDownloaderClient
{
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner( System.in );
        HttpClient httpClient = HttpClientBuilder.create().build();

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("~~~~~~~~~~~~~~~~~ Welcome to Youtube Downloader Client ~~~~~~~~~~~~~~~~");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("\n\n");

        System.out.println("Enter the Youtube Video URL : ");
        String youtubeURL = scanner.next().trim();
        System.out.println("\nRetrieving Video Information ...\n");
        if( youtubeURL.length() > 0 )
        {
            YoutubeVideoInfo videoInformation = DownloadUtils.getVideoInformation( httpClient, youtubeURL );
            if( videoInformation != null )
            {
                System.out.println();
                System.out.println("Video Title       : \n" + videoInformation.getVideoTitle());
                System.out.println();
                System.out.println("Video Description : \n" + videoInformation.getVideoDescription());

                try
                {
                    Map<String, String> downloadURLMap = DownloadUtils.getDownloadURLMap( videoInformation.getHtmlDoc() );
                    if( !downloadURLMap.isEmpty() )
                    {
                        System.out.println("\nIn what format would you like to download the video ? \n");
                        int encodingIdCount = 0;
                        for( String encoding : downloadURLMap.keySet() )
                        {
                            System.out.println( "[ " + ( ++encodingIdCount ) + " ] " + encoding );
                        }
                        System.out.println();
                        System.out.println(" Please enter the id of the encoding :");
                        int encodingId = scanner.nextInt();
                        if( encodingId <= downloadURLMap.keySet().size() )
                        {
                            Object[] encodingKeys = downloadURLMap.keySet().toArray();
                            if( encodingKeys[encodingId - 1] != null )
                            {
                                final String encodingKey = String.valueOf( encodingKeys[encodingId - 1] );
                                String downloadURL = downloadURLMap.get( encodingKey );
                                System.out.println("\n Video download URL : ");
                                System.out.println(downloadURL);

                                System.out.println("\n Video will be downloaded to current location");

                                String fileExtension = ".";
                                if( encodingKey.contains( "mp4" ) )
                                {
                                    fileExtension+= "mp4";
                                }
                                else if( encodingKey.contains( "webm" ) )
                                {
                                    fileExtension+= "webm";
                                }
                                else if( encodingKey.contains( "3gpp" ) )
                                {
                                    fileExtension+= "3gpp";
                                }

                                String downloadMessage = DownloadUtils.downloadVideo( httpClient, videoInformation.getVideoTitle().trim(), downloadURL, fileExtension );

                                System.out.println("\n ******************");
                                System.out.println(downloadMessage);
                            }
                        }
                    }
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            System.exit( 0 );
        }

    }
}
