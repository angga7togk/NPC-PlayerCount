package angga7togk.npcquery;

import angga7togk.bequery.BEQuery;
import angga7togk.bequery.BEQueryException;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntitySpawnEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NPCQuery extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().scheduleRepeatingTask(this, new QueryTask(this),20 * this.getConfig().getInt("query.schedule.update", 30), this.getConfig().getBoolean("query.schedule.async", true));
    }

    @EventHandler void onEntitySpawn(EntitySpawnEvent event){
        Entity entity = event.getEntity();
        this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
            @Override
            public void onRun() {
                setNameTagEntity(entity);
            }
        });
    }

    public void setNameTagEntity(Entity entity){
        boolean isNPC = entity.namedTag.getBoolean("npc");
        if (isNPC){
            String nameOld = entity.namedTag.contains("old-name") ? entity.namedTag.getString("old-name") : entity.getName();

            ResponseModel res = this.extractIPAndPortAndGetQuery(nameOld);
            if(res.getFormatFound() != null){
                BEQuery query = res.getQuery();
                String formated;
                if (query != null){
                    if (!entity.namedTag.contains("old-name")) entity.namedTag.putString("old-name", nameOld);
                    formated = this.getConfig().getString("query.format-online", "§aOnline: §b%players%§f/§e%max-players%")
                            .replace("%game-name%", String.valueOf(query.getGameName()))
                            .replace("%host-name%", String.valueOf(query.getHostName()))
                            .replace("%protocol%", String.valueOf(query.getProtocol()))
                            .replace("%version%", String.valueOf(query.getVersion()))
                            .replace("%players%", String.valueOf(query.getPlayers()))
                            .replace("%max-players%", String.valueOf(query.getMaxPlayers()))
                            .replace("%map%", String.valueOf(query.getMap()))
                            .replace("%game-mode%", String.valueOf(query.getGameMode()))
                            .replace("%nitendo-limited%", String.valueOf(query.getNintendoLimited()))
                            .replace("%ip-v4-port%", String.valueOf(query.getIpv4Port()))
                            .replace("%ip-v6-port%", String.valueOf(query.getIpv6Port()))
                            .replace("%extra%", String.valueOf(query.getExtra()));


                }else{
                    formated = this.getConfig().getString("query.format-offline", "§cServer Offline!");
                }
                entity.setNameTag(nameOld.replace(res.getFormatFound(), formated));
            }
        }
    }

    private ResponseModel extractIPAndPortAndGetQuery(String input){
        Pattern pattern = Pattern.compile("%(.*?):(\\d+)%");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {

            String ip = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));
            String text = matcher.group();
            try{
                ResponseModel res = new ResponseModel();
                res.setQuery(BEQuery.connect(ip, port));
                res.setFormatFound(text);
                return res;
            }catch (BEQueryException e) {
                ResponseModel res = new ResponseModel();
                res.setFormatFound(text);
                return res;
            }
        } else {
            return new ResponseModel();
        }
    }
}

@Data
class ResponseModel{
    private String formatFound = null;
    private BEQuery query = null;
}