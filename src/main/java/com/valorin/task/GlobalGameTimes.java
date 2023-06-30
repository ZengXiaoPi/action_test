package com.valorin.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GlobalGameTimes extends BukkitRunnable {
    private int times = -1;

    public int getValue() {
        return times;
    }

    public void run() {
        try {
            URL url = new URL(
                    "https://bstats.org/api/v1/plugins/6343/charts/duel_amount/data/?maxElements=100000");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; GTB7.5; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727)");
            InputStream in = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    StandardCharsets.UTF_8));
            String json = br.readLine();
            br.close();
            in.close();
            JsonArray jsonArray = new JsonParser().parse(json).getAsJsonArray();
            int times = 0;
            for (JsonElement jsonElement : jsonArray) {
                int data = ((JsonArray) jsonElement).get(1).getAsInt();
                //1000以上的视为虚假数据
                data = data > 1000 ? 0 : data;
                times += data;
            }
            this.times = times;
        } catch (Exception ignored) {
        }
    }
}
