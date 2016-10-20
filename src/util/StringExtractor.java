package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringExtractor {

    private final Pattern formatPattern;
    private final List<String> names;

    private StringExtractor(
            final Pattern formatPattern,
            final List<String> names) {
        ////
        this.formatPattern = formatPattern;
        this.names = names;
    }

    public static StringExtractor of(
            final String format,
            final String prefix,
            final String suffix) {
        ////
        return of(Pattern.compile("\\Q" + prefix + "\\E(.+?)\\Q" + suffix + "\\E"), format);
    }

    public static StringExtractor of(
            final String format) {
        ////
        return of(Pattern.compile("\\$\\{([^}]+)\\}"), format);
    }

    private static StringExtractor of(
            final Pattern fp,
            final String format) {
        ////
        final List<String> names = new ArrayList<>();
        final StringBuilder sb = new StringBuilder("\\Q" + format + "\\E");
        for (final Matcher m = fp.matcher(sb); m.find();) {
            names.add(m.group(1));
            m.region(m.start() + 8, sb.replace(m.start(), m.end(), "\\E(.+)\\Q").length());
        }
        return new StringExtractor(Pattern.compile(sb.toString()), names);
    }

    public Map<String, String> toMap(
            final String input) {
        ////
        final Matcher m = formatPattern.matcher(input);
        if (!m.matches())
            throw new IllegalArgumentException("Argument does not match format");
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < m.groupCount();)
            map.put(names.get(i), m.group(++i));
        return map;
    }

    public static void main(
            final String[] args) {
        ////
//        final StringExtractor r = of(TCCConstants.statusTemplate);
//        final String s = "<?xml version='1.0' encoding='ISO-8859-1'?> "
//        		+" <tcc_status> "
//        		+"   <overview> "
//        		+"     <error_string>blah</error_string> "
//        		+"   </overview> "
//        		+"   <coordinates> "
//        		+"     <RA>12:45:31.35</RA> "
//        		+"     <Dec>-88:56:33.4</Dec> "
//        		+"     <HA>6:19:10.2</HA> "
//        		+"     <Glat>-0.453464155975</Glat> "
//        		+"     <Glon>5.28620911537</Glon> "
//        		+"     <Alt>0.636015534401</Alt> "
//        		+"     <Az>3.14418578148</Az> "
//        		+"     <NS>-0.935129959926</NS> "
//        		+"     <EW>0.0</EW> "
//        		+"     <LMST>13:11:29.55</LMST> "
//        		+"   </coordinates> "
//        		+"   <ns> "
//        		+"     <error>None</error> "
//        		+"     <east> "
//        		+"       <tilt>-0.935093133362</tilt> "
//        		+"       <count>10345</count> "
//        		+"       <driving>False</driving> "
//        		+"       <state>disabled</state> "
//        		+"       <on_target>True</on_target> "
//        		+"       <system_status>112</system_status> "
//        		+"     </east> "
//        		+"     <west> "
//        		+"       <tilt>-0.933763344331</tilt> "
//        		+"       <count>10562</count> "
//        		+"       <driving>False</driving> "
//        		+"       <state>slow</state> "
//        		+"       <on_target>True</on_target> "
//        		+"       <system_status>112</system_status> "
//        		+"     </west> "
//        		+"   </ns> "
//        		+"   <md> "
//        		+"     <error>None</error> "
//        		+"     <east> "
//        		+"       <tilt>0.0</tilt> "
//        		+"       <count>8388608</count> "
//        		+"       <driving>False</driving> "
//        		+"       <state>auto</state> "
//        		+"       <on_target>True</on_target> "
//        		+"       <system_status>0</system_status> "
//        		+"     </east> "
//        		+"     <west> "
//        		+"       <tilt>0.0</tilt> "
//        		+"       <count>8388608</count> "
//        		+"       <driving>False</driving> "
//        		+"       <state>auto</state> "
//        		+"       <on_target>True</on_target> "
//        		+"       <system_status>0</system_status> "
//        		+"     </west> "
//        		+"   </md> "
//        		+" </tcc_status> ";
    	final StringExtractor r = of(""
                + "   <data>\n"
                + "     <id>${id}</id>\n"
                + "<name>${name}</name>\n"
                + "   </data>\n");
        final String s = ""
                + "   <data>\n"
                + "     <id>900</id>\n"
                + "     <name>Vivek</name>\n"
                + "   </data>\n";
        System.out.println(r.toMap(s)); // {name=Vivek, id=900}
    }
}