package com.wdbyte.bing;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.wdbyte.bing.html.WebSiteGenerator;

/**
 * @author niujinpeng
 * @date 2021/02/08
 * @link https://github.com/niumoo
 */
public class Wallpaper {

    // BING API
    private static String BING_API = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=10&nc=1612409408851&pid=hp&FORM=BEHPTB&uhd=1&uhdwidth=3840&uhdheight=2160";

    private static String BING_URL = "https://cn.bing.com";

    public static void main(String[] args) throws IOException {
        String httpContent = HttpUtls.getHttpContent(BING_API);
        JSONObject jsonObject = JSON.parseObject(httpContent);
        JSONArray jsonArray = jsonObject.getJSONArray("images");

        jsonObject = (JSONObject)jsonArray.get(0);
        // 图片地址
        String url = BING_URL + (String)jsonObject.get("url");
        url = url.substring(0, url.indexOf("&"));

        // 图片时间
        String enddate = (String)jsonObject.get("enddate");
        LocalDate localDate = LocalDate.parse(enddate, DateTimeFormatter.BASIC_ISO_DATE);
        enddate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 图片版权
        String copyright = (String)jsonObject.get("copyright");

        List<Images> imagesList = BingFileUtils.readBing();
        imagesList.set(0,new Images(copyright, enddate, url));
        imagesList = imagesList.stream().distinct().collect(Collectors.toList());
        BingFileUtils.writeBing(imagesList);
        BingFileUtils.writeReadme(imagesList);
        BingFileUtils.writeMonthInfo(imagesList);
        imagesList.forEach(ele -> {
            try {
                downLoad(ele.getUrl(),ele.getDate(),ele.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        new WebSiteGenerator().htmlGenerator();
    }

    public static void downLoad(String sUrl, String name, String datetime) throws Exception {
        //定义一个URL对象，就是你想下载的图片的URL地址
        URL url = new URL(sUrl);
        //打开连接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为10秒
        conn.setConnectTimeout(10 * 1000);
        //通过输入流获取图片数据
        InputStream is = conn.getInputStream();
        //得到图片的二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = readInputStream(is);
        //创建一个文件对象用来保存图片，默认保存当前工程根目录，起名叫Copy.jpg
        datetime = datetime.substring(0, 7);
        String pathName = "D:\\wallpaper" +"\\" + datetime + "\\"+ name +".jpg";
        File file = new File(pathName);
        if (file.getParentFile() != null && !file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        //创建输出流
        FileOutputStream outStream = new FileOutputStream(file);
        //写入数据
        outStream.write(data);
        //关闭输出流，释放资源
        outStream.close();
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[6024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }

}
