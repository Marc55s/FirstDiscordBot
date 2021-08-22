package musicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
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

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(event.getMember().equals(event.getGuild().getMember(User.fromId("325265696130990081"))))
            event.getGuild().addRoleToMember("325265696130990081", event.getGuild().getRoleById("769119865587630081")).queue();
        super.onGuildMemberJoin(event);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (b) {
            //hourly(event.getChannel());
        }
        b = false;

        String[] userInput = event.getMessage().getContentRaw().split(" ", 2);

        if (Objects.requireNonNull(event.getMember()).getVoiceState() != null)
            vc = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();

        for (int i = 0; i < commandHandler.getCommmands().size(); i++) {
            if (userInput[0].equals(commandHandler.getCommmands().get(i))) {
                //TODO: Second Player for mp3
                switch (i) {
                    case 0 -> event.getChannel().sendMessage(eb.build()).queue();
                    case 1 -> loadAndPlay(event.getChannel(), userInput[1]);
                    case 2 -> skipTrack(event.getChannel());
                    case 3 -> {
                        //TODO: Clear queue
                        event.getGuild().getAudioManager().closeAudioConnection();
                        event.getChannel().sendMessage("Disconnected").queue();
                    }
                    case 4 -> loadAndPlay(event.getChannel(), commandHandler.getSpecial().get(0));
                    case 5 -> loadAndPlay(event.getChannel(), commandHandler.getSpecial().get(1));
                    case 6 -> {
                        if (event.getMember().equals(event.getGuild().getMember(User.fromId("325265696130990081")))) break;
                        Objects.requireNonNull(event.getGuild().getMember(User.fromId("325265696130990081"))).kick().queue();
                    }
                    default -> event.getChannel().sendMessage("Not on the CommandList").queue();
                }
            }
        }
        super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue: " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(vc);
        }
    }

    private void hourly(TextChannel channel) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {

            //new TrackScheduler(getGuildAudioPlayer(channel.getGuild()).player).getPlayer().startTrack(true);
            channel.sendMessage("Bong ihr Missgeburten").queue();
            loadAndPlay(channel, commandHandler.getSpecial().get(0));
            connectToFirstVoiceChannel(channel.getGuild().getAudioManager());

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
                >play | >Bong
                >skip | >antiweeb
                """, false);
        eb.setImage("https://everythingisviral.com/wp-content/webp-express/webp-images/uploads/2020/10/polite-cat.png.webp");
        eb.setFooter("special: tom");
    }

}