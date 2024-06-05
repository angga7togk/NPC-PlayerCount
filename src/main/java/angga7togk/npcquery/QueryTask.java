package angga7togk.npcquery;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;

public class QueryTask implements Runnable {

    private final NPCQuery plugin;

    public QueryTask(NPCQuery plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Level level: this.plugin.getServer().getLevels().values()){
            for (Entity entity: level.getEntities()){
               this.plugin.setNameTagEntity(entity);
            }
        }
    }
}
