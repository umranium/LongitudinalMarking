/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.json;

import com.umranium.longmark.common.Log;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Umran
 */
public class JsonCommon {
    public static class TabsCache {
        final Map<Integer,String> tabs;

        public TabsCache() {
            this.tabs = new HashMap<Integer, String>(1000);

            StringBuilder sb = new StringBuilder();
            for (int i=0; i<100; ++i) {
                this.tabs.put(i, sb.toString());
                sb.append("\t");
            }
        }
        
        private String createTabs(int tabCount) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<tabCount; ++i)
                sb.append("\t");
            return sb.toString();
        }
        
        public String getTabs(int tabCount) {
            if (!tabs.containsKey(tabCount)) {
                synchronized (tabs) {
                    if (!tabs.containsKey(tabCount)) {
                        tabs.put(tabCount, createTabs(tabCount));
                    }
                }
            }
            return tabs.get(tabCount);
        }
    }
    
    public static final TabsCache TABS_CACHE = new TabsCache();
    
    public static StringBuilder printJsonLine(StringBuilder builder, int tabs, String line) {
        builder.append("\n").append(TABS_CACHE.getTabs(tabs)).append(line);
        return builder;
    }
    
    private static StringBuilder printObject(StringBuilder builder, int tabs, String name, Object value) {
        if (value==null) {
            //throw new RuntimeException("Null Object being printed to JSON.");
            builder.append("null");
        } else
            if (value instanceof java.util.List) {
                List list = (List)value;
                builder.append("\n").append(TABS_CACHE.getTabs(tabs+1)).append("[");
                boolean first = true;
                for (Object val:list) {
                    if (first)
                        first = false;
                    else
                        builder.append(",");
                    printObject(builder, tabs+1, name, val);
                }
                builder.append("\n").append(TABS_CACHE.getTabs(tabs+1)).append("]");
            } else
                if (value instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject)value;
                    jsonObject.printToJson(builder, tabs+1);
                } else
                    if (value instanceof Enum) {
                        builder.append("\"").append(value.toString().toLowerCase()).append("\"");
                    } else
                        if ((value instanceof Double) || (value instanceof Float)) {
                            if (value instanceof Double) {
                                Double d = (Double)value;
                                if (d.isInfinite() || d.isNaN()) {
                                    builder.append("\"").append(value).append("\"");
                                } else {
                                    builder.append(value);
                                }
                            } else
                                if (value instanceof Float) {
                                    Float f = (Float)value;
                                    if (f.isInfinite() || f.isNaN()) {
                                        builder.append("\"").append(value).append("\"");
                                    } else {
                                        builder.append(value);
                                    }
                                } else {
                                    builder.append(value.toString());
                                }
                        } else
                            if (value instanceof String) {
                                String str = (String)value;
                                String quotedStr = quoteText(str);
//                                StringBuilder sb = new StringBuilder();
//                                for (int i=0; i<str.length(); ++i)
//                                    sb.append("\t").append((int)str.charAt(i));
                                //out.println("'"+str+"' : '"+quotedStr+"' ["+sb.toString()+"]");
                                builder.append("\"").append(quotedStr).append("\"");
                            } else  {
                                builder.append(value.toString());
                            }
        return builder;
    }

    public static StringBuilder printJsonAttr(StringBuilder builder, int tabs, String name, Object value) {
        builder.append("\n").append(TABS_CACHE.getTabs(tabs));
        builder.append("\"").append(name).append("\": ");
        printObject(builder, tabs, name, value);
        return builder;
    }
    
    private static final Map<Character,String> QUOTE_CHAR_MAP = new HashMap<Character,String>() {
        {
            String slash = "\\";
            
            this.put('\"', slash+"\"");
            this.put('\'', slash+"\'");
            this.put('\\', slash+slash);
            this.put('/', slash+"/");
            this.put('\b', slash+"b");
            this.put('\f', slash+"f");
            this.put('\n', slash+"n");
            this.put('\r', slash+"r");
            this.put('\t', slash+"t");
        }
    };
    
    public static String quoteText(String text) {
//        text = text.replaceAll("\"", "\\\"");
//        text = text.replaceAll("\'", "\\\'");
//        text = text.replaceAll("\\\\", "\\\\\\\\");
//        text = text.replaceAll("/", "\\/");
//        text = text.replaceAll("\b", "\\\\b");
//        text = text.replaceAll("\f", "\\\\f");
//        text = text.replaceAll("\n", "\\\\n");
//        text = text.replaceAll("\r", "\\\\r");
//        text = text.replaceAll("\t", "\\\\t");
//        text = text.replaceAll("\u000B", "\\\\v");
        
        StringBuilder sb = new StringBuilder(text.length()*2);
        for (int i=0; i<text.length(); ++i) {
            char c = text.charAt(i);
            if (QUOTE_CHAR_MAP.containsKey(c)) {
                sb.append(QUOTE_CHAR_MAP.get(c));
            } else {
                if (c>=32 && c<=0x7F) {
                    sb.append(c);
                } else {
//                    String cVal = Integer.toHexString((int)c);
//                    if (cVal.length()<4) {
//                        cVal = String.format("%04x", (int)c);
//                    }
                    String cVal = String.format("%04x", (int)c);
                    sb.append("\\u").append(cVal);
                }
            }
        }
        
        return sb.toString();
    }
    
    public static void main(String[] args) {
        PrintWriter out = Log.out();
        out.println(quoteText("1 \rSeptember,  \r2012"));
    }
    
}
