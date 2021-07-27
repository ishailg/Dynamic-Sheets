import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Bot extends ListenerAdapter {
    static String HELP_MSG = "Syntax:```~calib <col>``` to calibrate your column```~rate <num>``` to rate today```~rate <num> <dd/mm>``` to rate dd/mm```~help``` to show this```~bloop``` to bloop```~whatis dd/MM```to check your rating of this date";
    private Map<String, String> columns = new HashMap<>();
    public static final LocalDateTime SEP_FIRST = Utils.getDateFromDM(1, 9);
    public static final int SEP_FIRST_OFFSET = 8;
    public static final int YEAR = 2020;
    public static void main(String[] args) throws LoginException {

        JDABuilder.createLight("YOUR-TOKEN-HERE", GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .setActivity(Activity.playing("Type ~help"))
                .build();


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        try {
            Message msg = event.getMessage();
            MessageChannel channel = event.getChannel();
            if (msg.getContentRaw().charAt(0) != '~') {
                return;
            }
            String[] tokens = msg.getContentRaw().split(" ");
            String name = msg.getAuthor().getName();
            switch (tokens[0]) {
                case "~alert":
                    channel.sendMessage("@rating dont forget to rate!").queue();
                    break;
                case "~help":
                    channel.sendMessage(HELP_MSG).queue();
                    break;
                case "~time":
                    String time = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
                    channel.sendMessage("It's " + time).queue(response -> channel.sendMessage("But its high noon somewhere in the world").queue());
                    break;
                case "~date":
                    String date = Utils.dateToString(LocalDateTime.now());
                    channel.sendMessage("today is " + date).queue();
                    channel.sendMessage("Btw, if you type '~rate' it will rate the " + Utils.getRatingDayString()).queue();
                    break;
                case "~bloop":
                    channel.sendMessage(name + " you are beiza").queue();
                    break;
                case "~calib":
                    if (tokens.length < 2) {
                        channel.sendMessage("invalid syntax ahi").queue();
                        break;
                    }
                    columns.put(name, tokens[1].substring(0, 1));
                    channel.sendMessage(String.format("updated %s for column %s", name, tokens[1].substring(0, 1))).queue();
                    break;
                case "~whatis": //tokens[1] = dd/MM
                    int days = Integer.parseInt(tokens[1].split("/")[0]);
                    int month = Integer.parseInt(tokens[1].split("/")[1]);
                    if (!columns.containsKey(name)) {
                        channel.sendMessage("you have to calibrate your column first").queue();
                        break;
                    }
                    String col = columns.get(name);
                    String rating = "N/A";
                    try {
                        rating = SheetsApi.getDateRating(col, Utils.getDateFromDM(days, month));
                    } catch (Exception e) {
                        e.printStackTrace();
                        channel.sendMessage("error").queue();
                        break;
                    }
                    channel.sendMessage("you rated the " + days + "/" + month + " as " + rating).queue();
                    break;
                case "~rate": // "~rate num" or "~rate num dd/MM"
                    if (!columns.containsKey(name)) {
                        channel.sendMessage("you have to calibrate your column first").queue();
                        break;
                    }
                    if (tokens.length == 2) { //~rate num
                        channel.sendMessage(Utils.parseRatingMessage(Utils.getRatingDay(), columns.get(name), tokens[1])).queue();
                        break;
                    }
                    if (tokens.length == 3) { //~rate num dd/MM
                        try{
                            channel.sendMessage(Utils.parseRatingMessage(Utils.getDateFromString(tokens[2]), columns.get(name), tokens[1])).queue();
                            break;
                        } catch (Exception e){
                            e.printStackTrace();
                            channel.sendMessage("erorr").queue();
                            break;
                        }

                    }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
