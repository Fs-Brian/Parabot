import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.SleepCondition;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.api.events.MessageEvent;
import org.rev317.min.api.events.listeners.MessageListener;
import org.rev317.min.api.methods.*;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

@ScriptManifest(
        author = "Brian",
        name = "PKH Hunter",
        category = Category.HUNTER,
        version = 1.0,
        description = "Hunts: Ruby Harvests, Black Warlocks, and Snowy Knights",
        servers = {"PKHonor"})

public class Hunter extends Script implements Paintable, MessageListener{

    private ArrayList<Strategy> strategies;

    private final int[] HUNTER_IDS = {0}; //placeholder for future additions
    private final int RUBY_ID = 5085;
    private final int SNOWY_ID = 5083;
    private final int BLACK_ID = 5082;

    private long startTime;
    private Timer runTime = new Timer();
    private boolean rubyCaught = false;
    private boolean snowyCaught = false;
    private boolean blackCaught = false;
    private int caughtCount = 0;


    @Override
    public boolean onExecute() {
        strategies = new ArrayList<>();

        JFrame f = new JFrame();
        JPanel p = new JPanel();
        f.setSize(350,100);
        f.setVisible(true);
        JButton a = new JButton ("Ruby Harvest");
        JButton b = new JButton ("Snow Knight");
        JButton c = new JButton ("Black Warlock");
        JLabel l = new JLabel("Start with a butterfly net equipped and in the correct area.");

        a.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                strategies.add(new RubyHarvest());
                f.dispose();
                runTime.restart();
            }
        });
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                strategies.add(new SnowyKnight());
                f.dispose();
                runTime.restart();
            }
        });
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                strategies.add(new BlackWarlock());
                f.dispose();
                runTime.restart();
            }
        });
        p.add(a);
        p.add(b);
        p.add(c);
        p.add(l);
        f.add(p);
        while (f.isVisible()){
            sleep(50);
        }

        strategies.add(new Drop());

        provide(strategies);
        return true;
    }
    @Override
    public void onFinish() {
        System.out.println("Script Stopped");
    }
    @Override
    public void messageReceived(MessageEvent m) {
        String message = m.getMessage().toLowerCase();
        if (m.getType() == 0){
            if(message.contains("you successfully catch a ruby harvest")){
                rubyCaught = true;
                caughtCount += 1;
            }else if(message.contains("you successfully catch a snowy knight")){
                snowyCaught = true;
                caughtCount += 1;
            }else if(message.contains("you successfully catch a black warlock")){
                blackCaught = true;
                caughtCount += 1;
            }
        }
    }

    public void paint(Graphics g1) {
        Graphics2D g = (Graphics2D)g1;
        g.drawString("Hunted Count: " + caughtCount + " (P/h: " + runTime.getPerHour(caughtCount) + ")", 8, 286);
        g.drawString("Run Time : " + runTime.toString(), 8, 325);
    }


    private class RubyHarvest implements Strategy {
        Npc ruby;

        @Override
        public boolean activate() {
            //if statement wasn't working properly.
            try {
                ruby = Npcs.getClosest(RUBY_ID);
            } catch (Exception e){
            }

            return ruby != null && Players.getMyPlayer().getAnimation() == -1;
        }

        @Override
        public void execute() {
            if(ruby != null){
                //No catch option in Npcs.Option
                ruby.interact(0);

                Time.sleep(new SleepCondition() {
                    @Override
                    public boolean isValid() {
                        return rubyCaught;
                    }
                }, 5000);;
                rubyCaught = false;
                Time.sleep(100,300);
            }
        }
    }

    private class SnowyKnight implements Strategy {
        Npc snowy = null;

        @Override
        public boolean activate() {
            //if statement wasn't working properly.
            try {
                snowy = Npcs.getClosest(SNOWY_ID);
            } catch (Exception e){
            }
            return snowy != null && Players.getMyPlayer().getAnimation() == -1;
        }

        @Override
        public void execute() {
            if(snowy != null){
                //No catch option in Npcs.Option
                snowy.interact(0);

                Time.sleep(new SleepCondition() {
                    @Override
                    public boolean isValid() {
                        return snowyCaught;
                    }
                }, 5000);;
                snowyCaught = false;
                Time.sleep(100,300);
            }
        }
    }

    private class BlackWarlock implements Strategy {
        Npc black = null;

        @Override
        public boolean activate() {
            //if statement wasn't working properly.
            try {
                black = Npcs.getClosest(BLACK_ID);
            } catch (Exception e){
            }
            return black != null && Players.getMyPlayer().getAnimation() == -1;
        }

        @Override
        public void execute() {
            if(black != null){
                //No catch option in Npcs.Option
                black.interact(0);

                Time.sleep(new SleepCondition() {
                    @Override
                    public boolean isValid() {
                        return blackCaught;
                    }
                }, 5000);;
                blackCaught = false;
                Time.sleep(100,300);
            }
        }
    }

    private class Drop implements Strategy {
        @Override
        public boolean activate() {
            return Inventory.isFull();
        }

        @Override
        public void execute() {
            for(Item log : Inventory.getItems(HUNTER_IDS)){
                if(log != null){
                    int logCount = Inventory.getCount(HUNTER_IDS);
                    log.drop();
                    Time.sleep(new SleepCondition() {
                        @Override
                        public boolean isValid() {
                            return logCount > Inventory.getCount(HUNTER_IDS);
                        }
                    }, 3000);
                }
            }
        }
    }
}
