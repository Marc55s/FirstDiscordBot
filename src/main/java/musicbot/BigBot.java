package musicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BigBot extends ListenerAdapter {

    private final CommandHandler commandHandler;
    static VoiceChannel vc;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final EmbedBuilder eb;
    private boolean b = true;
    private String trackUrl;

    public static void main(String[] args) throws IllegalArgumentException, LoginException {
        JDABuilder.createDefault("ODc3NjMxNjQxMDYzOTE1NTQw.YR1cKA.4xxbd9Z_BgLInY3E-OHpMrwwiWg") // Use token provided as JVM argument
                .addEventListeners(new BigBot())
                .setActivity(Activity.playing("Thats a lot of Damage")) // Register new MusicBot instance as EventListener
                .build(); // Build JDA - connect to discord
    }

    BigBot() {
        eb = new EmbedBuilder();
        commandHandler = new CommandHandler();
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        initEmbed();
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    private synchronized GuildMusicManager getGuildAudioPlayer2(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        super.onGuildMemberJoin(event);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        event.getGuild().addRoleToMember("325265696130990081", Objects.requireNonNull(event.getGuild().getRoleById("769119865587630081"))).queue();
        if (b) {
            hourly(event.getChannel());
        }
        b = false;

        String[] userInput = event.getMessage().getContentRaw().split(" ", 2);

        if (Objects.requireNonNull(event.getMember()).getVoiceState() != null)
            vc = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();

        for (int i = 0; i < commandHandler.getCommmands().size(); i++) {
            if (userInput[0].equals(commandHandler.getCommmands().get(i))) {
                switch (i) {
                    case 0 -> event.getChannel().sendMessage(eb.build()).queue();
                    case 1 -> {
                        //TODO: Clear queue
                        event.getGuild().getAudioManager().closeAudioConnection();
                        event.getChannel().sendMessage("Disconnected").queue();
                        getGuildAudioPlayer(event.getGuild()).scheduler.nextTrack();
                        trackUrl = "";
                    }
                    case 2 -> loader(event.getGuild(), commandHandler.getSpecial().get(0));
                    case 3 -> loader(event.getGuild(), commandHandler.getSpecial().get(1));
                    case 4 -> {
                        if (event.getMember().equals(event.getGuild().getMember(User.fromId("325265696130990081"))))
                            break;
                        Objects.requireNonNull(event.getGuild().getMember(User.fromId("325265696130990081"))).kick().queue();
                    }
                    default -> event.getChannel().sendMessage("Not on the CommandList").queue();
                }
            }
        }


        trackUrl = "";
        super.onGuildMessageReceived(event);
    }

    public void loader(Guild guild, String trackUrl) {
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(guild, getGuildAudioPlayer(guild), track);
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });

    }


    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }


    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }

    private void hourly(TextChannel channel) {

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            String trackUrl = commandHandler.getSpecial().get(0);
            loader(channel.getGuild(),trackUrl);

        }, 0, 1, TimeUnit.HOURS);
    }

    private void initEmbed() {
        eb.setColor(Color.red);
        eb.setColor(new Color(0xF40C0C));
        eb.setColor(new Color(255, 0, 54));
        eb.setTitle("Cool Bot");
        eb.setDescription("Help Window");
        eb.addField("The best i can do is:", """
                >help | >leave
                >Bong
                >antiweeb
                """, false);
        eb.setImage("https://everythingisviral.com/wp-content/webp-express/webp-images/uploads/2020/10/polite-cat.png.webp");
        eb.setFooter("special: tom");
    }

}
