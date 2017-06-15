package mysqlgarbagecollector.red.man10;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import red.man10.man10mysqlapi.MySQLAPI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class man10 extends JavaPlugin {

    MySQLAPI mysql = null;
    String mySqlUser = null;
    String mySqlDb = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        mysql = new MySQLAPI(this,"MysqlGC");
        mySqlDb = mysql.getDB();
        mySqlUser = mysql.getUSER();
        onStartTimer();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void onStartTimer(){
        new BukkitRunnable(){

            @Override
            public void run() {
                clearMySqlMemory();
            }
        }.runTaskTimerAsynchronously(this,0,36000);
    }

    String prefix = "§e§l[MySQLGC]§f§l";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("mysqlgc")){
            if(args.length == 0){
                help(sender);
                return false;
            }
            if(args.length == 1){
                if(args[0].equals("help")){
                    help(sender);
                    return false;
                }
                if(args[0].equalsIgnoreCase("free")){
                    if(!sender.hasPermission("man10.mysqlgc.free")){
                        sender.sendMessage(prefix + "あなたには権限がありません");
                        return false;
                    }
                    sender.sendMessage(prefix + "MySqlのメモリを解放中・・・");
                    clearMySqlMemory();
                    sender.sendMessage(prefix + "解放完了");
                    return true;
                }
                if(args[0].equalsIgnoreCase("ammount")){
                    if(!sender.hasPermission("man10.mysqlcg.ammount")){
                        sender.sendMessage(prefix + "あなたには権限がありません");
                        return false;
                    }
                    ResultSet rs = mysql.query("SELECT COUNT(ID) FROM information_schema.processlist WHERE COMMAND ='Sleep'");
                    try {
                        while (rs.next()){
                            sender.sendMessage(prefix + "消去待ちプロセス数:" + rs.getString("COUNT(ID)"));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                help(sender);
                return false;
            }
            help(sender);
        }
        return false;
    }
    void help(CommandSender p){
        p.sendMessage("§e§l===[MySQLGarbageCollector]===");
        p.sendMessage("§e§l/mysqlgc ammount 解放可能数を表示");
        p.sendMessage("§e§l/mysqlgc free MySQLセッション解放");
        p.sendMessage("§e§l===========================");
        p.sendMessage("§e§lCreated By Sho0");
        return;
    }
    void clearMySqlMemory(){
            ResultSet r1 = mysql.query("select concat('KILL ',id,';') from information_schema.processlist where user = '" + mySqlUser + "' and DB ='" + mySqlDb + "' and COMMAND ='Sleep' limit 5000;");
            try {
                while (r1.next()){
                    mysql.execute(r1.getString("concat('KILL ',id,';')"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}
