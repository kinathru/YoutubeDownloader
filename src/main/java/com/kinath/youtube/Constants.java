package com.kinath.youtube;

public class Constants
{
    // Common Constants
    public static final String SCRIPT_TAG = "script";
    public static final String COMMA_SPLITTER = ",";
    public static final String SEMI_COLON_SPLITTER = ";";
    public static final String AND_SPLITTER = "&";
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String HTTPS = "https";

    public static final int MAX_URL_ATTEMPTS = 3;

    // YT Constants
    public static final String YT_PLAYER_CONTAINER_ID = "player-mole-container";
    public static final String YT_HEADLINE_TITLE_ID = "watch-headline-title";
    public static final String YT_EOW_TITLE_ID = "eow-title";
    public static final String YT_EOW_DESCRIPTION_ID = "eow-description";

    public static final String YT_PLAYER_SCRIPT_START_TAG = "var ytplayer = ytplayer";
    public static final String YT_PLAYER_CONFIG_START_TAG = "ytplayer.config = ";
    public static final String YT_PLAYER_CONFIG_END_TAG = "};";
    public static final String YT_PLAYER_CONFIG_ARGS = "args";
    public static final String YT_PLAYER_FORMAT_STREAM_MAP = "url_encoded_fmt_stream_map";

    public static final String YT_LINK_QUERY_PARAM_TYPE = "type";
    public static final String YT_LINK_QUERY_PARAM_ITAG = "itag";

}
