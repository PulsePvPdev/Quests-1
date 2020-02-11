package com.leonardobishop.quests.player;

import com.leonardobishop.quests.api.enums.StoreType;
import com.leonardobishop.quests.player.questprogressfile.QuestProgress;
import com.leonardobishop.quests.player.questprogressfile.QuestProgressFile;
import com.leonardobishop.quests.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.Quests;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class QPlayerManager {

    private final Quests plugin;

    public QPlayerManager(Quests plugin) {
        this.plugin = plugin;
    }

    private final ConcurrentMap<UUID, QPlayer> qPlayers = new ConcurrentHashMap<>();

    public QPlayer getPlayer(UUID uuid) {
        return qPlayers.get(uuid);
    }

    public void removePlayer(UUID uuid) {
        qPlayers.remove(uuid).getQuestProgressFile().saveToDisk();
    }

    public Collection<QPlayer> getQPlayers() {
        return qPlayers.values();
    }

    //Better to use the second method, so we know if we only load the data
    //public void loadPlayer(UUID uuid) {
    //   loadPlayer(uuid, false);
    //}

    public void loadPlayer(UUID uuid, boolean onlyData, StoreType storeType) {
        if (getPlayer(uuid) == null || getPlayer(uuid).isOnlyDataLoaded()) {
            QuestProgressFile questProgressFile = new QuestProgressFile(uuid, plugin);

            if (storeType != StoreType.YAML) { // Load from mysql
                try {
                    PreparedStatement progressStatement = this.plugin.getDatabase().getConnection().prepareStatement("SELECT * FROM " + this.plugin.getDatabase().getTablePrefix() == null ? "" : this.plugin.getDatabase().getTablePrefix()
                            + "progress WHERE player-uuid='" + uuid.toString() + "';");
                    ResultSet progressResult = progressStatement.executeQuery();
                    if (progressStatement != null)
                        progressStatement.close(); // finished the job
                    while (progressResult.next()) {
                        String id = progressResult.getString("quest-id");
                        boolean started = progressResult.getBoolean("started");
                        boolean completed = progressResult.getBoolean("completed");
                        boolean completedBefore = progressResult.getBoolean("completed_before");
                        long completionDate = progressResult.getLong("completition_date");

                        QuestProgress questProgress = new QuestProgress(id, completed, completedBefore, completionDate, uuid, started, true);

                        PreparedStatement progressTaskStatement = this.plugin.getDatabase().getConnection().prepareStatement("SELECT * FROM " + this.plugin.getDatabase().getTablePrefix() == null ? "" : this.plugin.getDatabase().getTablePrefix()
                                + "task-progress WHERE player-uuid='" + uuid.toString() + "' AND quest-id='" + id + "';");
                        ResultSet progressTaskResult = progressTaskStatement.executeQuery();
                        if (progressTaskStatement != null)
                            progressTaskResult.close(); //finished the job
                        while (progressTaskResult.next()) {

                        }
                    }
                    if (progressResult != null)
                        progressResult.close(); //finished the job
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Failed to load player: " + uuid + "! This WILL cause errors.");
                    ex.printStackTrace(); //expected during sql fails (when database is not setup properly)
                }

            } else { // Load file
                try {
                    File directory = new File(plugin.getDataFolder() + File.separator + "playerdata");
                    if (directory.exists() && directory.isDirectory()) {
                        File file = new File(plugin.getDataFolder() + File.separator + "playerdata" + File.separator + uuid.toString() + ".yml");
                        if (file.exists()) {
                            YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
                            if (data.isConfigurationSection("quest-progress")) { //Same job as "isSet" + it checks if is CfgSection
                                for (String id : data.getConfigurationSection("quest-progress").getKeys(false)) {
                                    boolean started = data.getBoolean("quest-progress." + id + ".started");
                                    boolean completed = data.getBoolean("quest-progress." + id + ".completed");
                                    boolean completedBefore = data.getBoolean("quest-progress." + id + ".completed-before");
                                    long completionDate = data.getLong("quest-progress." + id + ".completion-date");

                                    QuestProgress questProgress = new QuestProgress(id, completed, completedBefore, completionDate, uuid, started, true);

                                    for (String taskid : data.getConfigurationSection("quest-progress." + id + ".task-progress").getKeys(false)) {
                                        boolean taskCompleted = data.getBoolean("quest-progress." + id + ".task-progress." + taskid + ".completed");
                                        Object taskProgression = data.get("quest-progress." + id + ".task-progress." + taskid + ".progress");

                                        TaskProgress taskProgress = new TaskProgress(taskid, taskProgression, uuid, taskCompleted, false);
                                        questProgress.addTaskProgress(taskProgress);
                                    }

                                    questProgressFile.addQuestProgress(questProgress);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    plugin.getLogger().severe("Failed to load player: " + uuid + "! This WILL cause errors.");
                    ex.printStackTrace();
                    // fuck
                }

                QPlayer qPlayer = new QPlayer(uuid, questProgressFile, onlyData, plugin);

                this.qPlayers.put(uuid, qPlayer);
            }
        }
    }
}
