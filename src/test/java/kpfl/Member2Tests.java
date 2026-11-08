package kpfl;

import kpfl.exception.ValidationException;
import kpfl.model.Coach;
import kpfl.model.FootballTeam;
import kpfl.model.Player;

/** Small checks for the Team & Personnel classes. Run: java -cp out kpfl.Member2Tests */
public class Member2Tests {
    private static int passed;

    public static void main(String[] args) {
        FootballTeam first = new FootballTeam("T1", "Harbour FC", "Kingston");
        FootballTeam second = new FootballTeam("T2", "Hill FC", "St Andrew");
        Player player = new Player("P1", "Alex Brown", Player.MIN_AGE, "F", "Forward", 9);
        Coach coach = new Coach("C1", "Jordan Green", 35, "M", "", "Head Coach");

        check("minimum age accepted", player.getAge() == Player.MIN_AGE);
        check("younger player rejected", rejects(() -> new Player("P2", "Young Player", Player.MIN_AGE - 1, "M", "Forward", 8)));
        check("invalid player age change rejected", rejects(() -> player.setAge(Player.MIN_AGE - 1)));
        check("blank person name rejected", rejects(() -> player.setName("  ")));
        check("blank gender rejected", rejects(() -> coach.setGender(null)));
        check("invalid jersey rejected", rejects(() -> player.setJerseyNumber(100)));
        check("negative goals rejected", rejects(() -> player.setGoals(-1)));
        check("null status rejected", rejects(() -> player.setPlayerStatus(null)));
        check("unknown player status rejected", rejects(() -> player.setPlayerStatus("BENCHED")));
        player.setPlayerStatus("injured");
        check("player status saved in uppercase", player.getPlayerStatus().equals("INJURED"));
        check("blank parish rejected", rejects(() -> first.setParish(" ")));
        check("null team status rejected", rejects(() -> first.setStatus(null)));
        check("unknown team status rejected", rejects(() -> first.setStatus("PLAYING")));
        first.setStatus("active");
        check("approved team can play", first.isEligibleForFixture());

        first.addPlayer(player);
        first.addCoach(coach);
        check("team assignments recorded", first.getSquadSize() == 1
                && first.getCoaches().size() == 1
                && player.getTeamRegNo().equals("T1") && coach.getTeamRegNo().equals("T1"));
        check("second team cannot claim player", rejects(() -> second.addPlayer(player)));
        check("second team cannot claim coach", rejects(() -> second.addCoach(coach)));
        check("jersey cannot change while assigned", rejects(() -> player.setJerseyNumber(10)));
        check("duplicate jersey rejected", rejects(() -> first.addPlayer(
                new Player("P3", "Sam White", 20, "M", "Midfielder", 9))));
        check("player and coach cannot share an ID", rejects(() -> first.addCoach(
                new Coach("P1", "Another Coach", 40, "F", "", "Assistant"))));
        first.getPlayers().clear();
        check("changing a returned list does not change the squad", first.getSquadSize() == 1);

        first.removePlayer("P1");
        first.removeCoach("C1");
        player.setJerseyNumber(10);
        second.addPlayer(player);
        second.addCoach(coach);
        check("people can transfer after removal", first.getSquadSize() == 0
                && first.getCoaches().isEmpty() && second.getSquadSize() == 1
                && second.getCoaches().size() == 1);
        System.out.println(passed + " Team & Personnel checks passed.");
    }

    private static boolean rejects(Runnable action) {
        try { action.run(); return false; }
        catch (ValidationException expected) { return true; }
    }

    private static void check(String label, boolean ok) {
        if (!ok) throw new AssertionError(label);
        passed++;
        System.out.println("PASS  " + label);
    }
}
