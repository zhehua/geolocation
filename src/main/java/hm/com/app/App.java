package hm.com.app;

import io.vertx.core.Vertx;
import io.vertx.redis.client.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        String GEO_KEY="location";
        Vertx vertx = Vertx.vertx();
        vertx.createHttpServer().requestHandler(request -> {
            String longitude = request.getParam("longitude");
            String latitude = request.getParam("latitude");
            String nickname = request.getParam("nickname");

            Redis.createClient(vertx, new RedisOptions())
                    .connect(onConnect -> {
                        if (onConnect.succeeded()) {
                            RedisConnection client = onConnect.result();
                            RedisAPI redis = RedisAPI.api(client);
                            //保存用户位置
                            //GEOADD key longitude latitude nickname
                            redis.geoadd(Arrays.asList(new String[] {GEO_KEY,longitude,latitude,nickname}), res -> {
                                if (res.succeeded()) {
                                    System.out.println("添加成功..." + res.result().toInteger());
                                }
                            });
                            //获取附近的人
                            //GEORADIUS key longitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count]
                            redis.georadius(Arrays.asList(new String[] {GEO_KEY,longitude,latitude,"20","km"}), res -> {
                                if (res.succeeded()) {
                                    List<Response> list1=res.result().stream().filter(s2-> !s2.toString().equals(nickname)).collect(Collectors.toList());
                                    request.response().end(list1.toString());
                                }
                            });
                        }
                    });

        }).listen(8080);
    }
}
