package skh6075.playinglog;

import com.google.common.base.Charsets;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public final class PlayingLog extends JavaPlugin implements Listener {
    public final static String prefix = "§l§6 [!]§r§7 ";

    private String logDataPath;
    private File blockPlaceDataFolder;
    private File blockBreakDataFolder;
    private File chestTransactionDataFolder;

    @Override
    public void onEnable() {
        logDataPath = this.getDataFolder() + "log/";
        File logDataFolder = new File(logDataPath);
        if(!logDataFolder.exists() && logDataFolder.mkdir()){
            getLogger().log(Level.INFO, logDataPath + "로그 데이터 폴더를 생성했습니다.");
        }
        prepareLogDataFolders();
    }

    private void prepareLogDataFolders(){
        blockPlaceDataFolder = new File(logDataPath + "place/");
        blockBreakDataFolder = new File(logDataPath + "break/");
        chestTransactionDataFolder = new File(logDataPath + "chest/");
        if(!blockPlaceDataFolder.exists() && blockPlaceDataFolder.mkdir()){
            getLogger().log(Level.INFO, blockPlaceDataFolder + " 블록설치 데이터 폴더를 생성했습니다.");
        }
        if(!blockBreakDataFolder.exists() && blockBreakDataFolder.mkdir()){
            getLogger().log(Level.INFO, blockBreakDataFolder + " 블록파괴 데이터 폴더를 생성했습니다.");
        }
        if(!chestTransactionDataFolder.exists() && chestTransactionDataFolder.mkdir()){
            getLogger().log(Level.INFO, chestTransactionDataFolder + " 상자로그 데이터 폴더를 생성했습니다.");
        }
    }

    private String pos2hash(Location location){
        return location.getBlockX()+":"+location.getBlockY()+":"+location.getBlockZ()+":"+location.getWorld().getName();
    }

    private Boolean recordBlockLog(String path, String hash, Player player) throws IOException {
        File tmpFile = new File(blockPlaceDataFolder.getPath() + hash + ".yml");
        Boolean created = tmpFile.createNewFile();

        YamlConfiguration tmpConfig = YamlConfiguration.loadConfiguration(tmpFile);
        InputStream tmpConfigStream = getResource(tmpFile.getPath());
        if(tmpConfigStream == null){
            return false;
        }
        tmpConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(tmpConfigStream, Charsets.UTF_8)));
        tmpConfig.set(hash, player.getName());
        tmpConfig.save(tmpFile);
        return true;
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) throws IOException {
        if(!event.isCancelled()){
            Player player = event.getPlayer();
            Block block = event.getBlockPlaced();
            String hash = pos2hash(block.getLocation());
            if(block.getType() == Material.CHEST){
                File tmpFile = new File(chestTransactionDataFolder.getPath() + hash + ".yml");
                if(!tmpFile.createNewFile()){
                    event.setCancelled(true);
                    return;
                }
                player.sendMessage(prefix + "상자 로그 데이터를 생성했습니다.");
            }else{
                Boolean recorded = recordBlockLog(blockPlaceDataFolder.getPath() + hash + ".yml", hash, player);
                event.setCancelled(!recorded);
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) throws IOException {
        if(!event.isCancelled()){
            Player player = event.getPlayer();
            Block block = event.getBlock();
            String hash = pos2hash(block.getLocation());
            if(block.getType() == Material.CHEST){
                File tmpFile = new File(chestTransactionDataFolder.getPath() + hash + ".yml");
                if(tmpFile.exists()){
                    if(player.isOp() && tmpFile.delete()){
                        player.sendMessage(prefix + "상자 로그 데이터 파일이 삭제됐습니다.");
                    }
                }
            }else{
                Boolean recorded = recordBlockLog(blockBreakDataFolder.getPath() + hash + ".yml", hash, player);
                event.setCancelled(!recorded);
            }
        }
    }
}
