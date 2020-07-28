package org.glycoinfo.GlycanFormatConverter.util;

import java.io.*;

public class DataFileReader {

    private final String m_strOutput;

    public DataFileReader (final String a_objFilepath) {
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(a_objFilepath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            while((str = br.readLine()) != null){
                String strline = this.removeTabReturn(str);

                if (strline.getBytes().length > 0) {
                    sb.append(str).append("\n");
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        this.m_strOutput = sb.toString();
    }

    public String getFileStrings() {
        return this.m_strOutput;
    }

    private String removeTabReturn (String a_Str) {
        String str_out = "";
        String strline0 = a_Str.replaceAll("\\t", "");
        String strline1 = strline0.replaceAll("\\r\\n", "");
        str_out = strline1.replaceAll("\\n", "");
        return str_out;
    }
}
