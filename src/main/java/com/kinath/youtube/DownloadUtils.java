package com.kinath.youtube;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.kinath.youtube.Constants.*;

public class DownloadUtils
{

    static Logger logger = Logger.getLogger( "ytd" );

    public static YoutubeVideoInfo getVideoInformation( HttpClient httpClient, String youtubeURL )
    {
        if( httpClient != null )
        {
            YoutubeVideoInfo youtubeVideoInfo = new YoutubeVideoInfo();

            HttpRequest httpRequest = new HttpGet( youtubeURL );
            httpRequest.addHeader( "User-Agent", HTTP.USER_AGENT );

            try
            {
                HttpResponse httpResponse = httpClient.execute( (HttpUriRequest) httpRequest );

                HttpEntity responseEntity = httpResponse.getEntity();
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( responseEntity.getContent() ) );

                StringBuilder result = new StringBuilder();
                String line = null;
                while( ( line = bufferedReader.readLine() ) != null )
                {
                    result.append( line );
                }
                bufferedReader.close();

                Document htmlDoc = Jsoup.parse( result.toString() );
                youtubeVideoInfo.setHtmlDoc( htmlDoc );

                Element watchHeadLineElement = htmlDoc.getElementById( YT_HEADLINE_TITLE_ID );
                if( watchHeadLineElement != null )
                {
                    Element titleElement = watchHeadLineElement.getElementById( YT_EOW_TITLE_ID );
                    if( titleElement != null )
                    {
                        for( Node node : titleElement.childNodes() )
                        {
                            if( node instanceof TextNode )
                            {
                                youtubeVideoInfo.setVideoTitle( ( (TextNode) node ).getWholeText() );
                                break;
                            }
                        }

                    }
                }

                Element descriptionElement = htmlDoc.getElementById( YT_EOW_DESCRIPTION_ID );
                if( descriptionElement != null )
                {
                    List<Node> descriptionNodes = descriptionElement.childNodes();
                    StringBuilder descriptionSb = new StringBuilder();
                    for( Node descriptionNode : descriptionNodes )
                    {
                        if( descriptionNode instanceof TextNode )
                        {
                            descriptionSb.append( ( (TextNode) descriptionNode ).getWholeText() );
                        }
                        else if( descriptionNode instanceof Element && ( (Element) descriptionNode ).tagName().equals( "br" ) )
                        {
                            descriptionSb.append( "\n" );
                        }
                        else if( descriptionNode instanceof Element && ( (Element) descriptionNode ).tagName().equals( "a" ) )
                        {
                            descriptionSb.append( descriptionNode.attributes().toString() );
                        }
                    }
                    youtubeVideoInfo.setVideoDescription( descriptionSb.toString() );
                }

                return youtubeVideoInfo;
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String downloadVideo( HttpClient httpClient, String downloadFileName, String downloadUrl, String encoding ) throws IOException
    {
        String returnMessage = null;
        HttpGet httpget2 = new HttpGet( downloadUrl );
        httpget2.setHeader( "User-Agent", HTTP.USER_AGENT );

        HttpResponse response2 = httpClient.execute( httpget2 );
        HttpEntity entity2 = response2.getEntity();

        if( entity2 != null && response2.getStatusLine().getStatusCode() == 200 )
        {
            long length = entity2.getContentLength();
            InputStream instream2 = entity2.getContent();
            downloadFileName += encoding;
            FileOutputStream outstream = new FileOutputStream( downloadFileName );
            try
            {
                byte[] buffer = new byte[2048];
                int count = -1;
                while( ( count = instream2.read( buffer ) ) != -1 )
                {
                    outstream.write( buffer, 0, count );
                }
                returnMessage = "Downloaded Successfully!!!";
                outstream.flush();
            }
            finally
            {
                outstream.close();
            }
        }
        else
        {
            returnMessage = response2.getStatusLine().getStatusCode() + " " + response2.getStatusLine().getReasonPhrase();
        }

        return returnMessage;
    }

    public static Map<String, String> getDownloadURLMap( Document htmlDoc ) throws IOException
    {
        Element playerElement = htmlDoc.getElementById( YT_PLAYER_CONTAINER_ID );
        Elements scriptElements = playerElement.getElementsByTag( SCRIPT_TAG );

        Map<String, String> downloadUrlStringList = new HashMap<String, String>();
        for( Element scriptElement : scriptElements )
        {
            Node node = scriptElement.childNode( 0 );
            if( node instanceof DataNode )
            {
                DataNode ytPlayerNode = (DataNode) node;
                String wholeData = ytPlayerNode.getWholeData();
                if( wholeData.startsWith( YT_PLAYER_SCRIPT_START_TAG ) )
                {

                    String playerStartString = YT_PLAYER_CONFIG_START_TAG;
                    int configStartIndex = wholeData.indexOf( playerStartString ) + playerStartString.length();
                    String tempString = wholeData.substring( configStartIndex, wholeData.length() );
                    int configEndIndex = tempString.indexOf( YT_PLAYER_CONFIG_END_TAG );
                    String configString = tempString.substring( 0, configEndIndex + 1 );

                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> configMap = new HashMap<String, Object>();

                    // convert JSON string to Map
                    configMap = objectMapper.readValue( configString, new TypeReference<Map<String, Object>>()
                    {
                    } );
                    Object argsMapObj = configMap.get( YT_PLAYER_CONFIG_ARGS );
                    if( argsMapObj instanceof Map )
                    {
                        Map argsMap = (Map) argsMapObj;
                        Object url_encoded_fmt_stream_map = argsMap.get( YT_PLAYER_FORMAT_STREAM_MAP );
                        if( url_encoded_fmt_stream_map != null )
                        {
                            String downloadUrlsString = String.valueOf( url_encoded_fmt_stream_map );
                            String[] splitUrlStringArray = downloadUrlsString.split( COMMA_SPLITTER );


                            for( String splitTempUrl : splitUrlStringArray )
                            {
                                logger.info( "Initial URL : " + splitTempUrl );

                                String decodedURL = java.net.URLDecoder.decode( splitTempUrl, UTF_8_ENCODING );
                                String[] urlPartsArray = decodedURL.split( SEMI_COLON_SPLITTER );

                                String url = null;
                                String type = null;

                                for( String urlPart : urlPartsArray )
                                {
                                    if( urlPart.contains( HTTPS ) )
                                    {
                                        String urlTmp = urlPart.substring( urlPart.indexOf( HTTPS ), urlPart.length() );
                                        String[] queryStringArray = urlTmp.split( AND_SPLITTER );
                                        StringBuffer sb = new StringBuffer();
                                        for( String qsa : queryStringArray )
                                        {
                                            if( qsa.startsWith( YT_LINK_QUERY_PARAM_TYPE ) )
                                            {
                                                type = qsa;
                                            }
                                            break;
                                        }
                                        url = urlTmp;
                                    }

                                    if( urlPart.contains( YT_LINK_QUERY_PARAM_TYPE ) )
                                    {
                                        String[] typeSplit = urlPart.split( AND_SPLITTER );
                                        for( String typePart : typeSplit )
                                        {
                                            if( typePart.startsWith( YT_LINK_QUERY_PARAM_TYPE ) )
                                            {
                                                type = typePart;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if( url != null && type != null )
                                {
                                    logger.log( Level.INFO, "URL Before Validation : " + url );
                                    String validatedURL = furtherURLValidation( url );
                                    logger.log( Level.INFO, "URL After Validation : " + validatedURL );
                                    downloadUrlStringList.put( type, validatedURL );
                                }
                            }
                        }
                    }

                    break;
                }
            }
        }

        return downloadUrlStringList;
    }

    public static String furtherURLValidation( String url )
    {
        String[] splitForQueryParams = url.split( AND_SPLITTER );
        StringBuilder newUrlSb = new StringBuilder();
        newUrlSb.append( splitForQueryParams[0] ).append( AND_SPLITTER );

        int itagCount = 0;

        for( int i = 1; i < splitForQueryParams.length; i++ )
        {
            String queryParam = splitForQueryParams[i];
            if( queryParam.startsWith( YT_LINK_QUERY_PARAM_ITAG ) )
            {
                itagCount++;
                if( itagCount == 1 )
                {
                    newUrlSb.append( queryParam ).append( AND_SPLITTER );
                }
                continue;
            }
            else if( queryParam.startsWith( YT_LINK_QUERY_PARAM_TYPE ) )
            {
                continue;
            }
            newUrlSb.append( queryParam ).append( AND_SPLITTER );
        }

        return newUrlSb.toString();
    }
}
