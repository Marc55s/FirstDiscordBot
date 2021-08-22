import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.Date;

public class Bot extends ListenerAdapter {

    static JDA jda;

    public static void main(String[] args) throws LoginException {
        // args[0] should be the token
        // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
        // All other events will be disabled.

        JDABuilder.createLight("ODc3NjMxNjQxMDYzOTE1NTQw.YR1cKA.4xxbd9Z_BgLInY3E-OHpMrwwiWg", GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .setActivity(Activity.playing("Bitches Buttern"))
                .build();


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+"); // gets message from mod and splits every whitespace.

        //error message
          User target = Bot.jda.getUserById("325265696130990081");
        for (String str : args) {
            if (str.contains("thomas")) {
              //  event.getGuild().ban(User.fromId("325265696130990081"), 0, "ban command").queue();
            }
            if (str.contains("tom")) {
               event.getGuild().kick("325265696130990081").queue();
                event.getChannel().sendMessage("Tom ist genug gekickt geworden").queue();
            }
        }

        if (args[0].equalsIgnoreCase("!s")) {
           //TODO: if tom not banned give role

            event.getGuild().unban(User.fromId("325265696130990081")).queue();
            Role r = event.getGuild().getRoleById("769119865587630081");
            assert r != null;
            event.getGuild().addRoleToMember("325265696130990081",r);
        }
    }
}
