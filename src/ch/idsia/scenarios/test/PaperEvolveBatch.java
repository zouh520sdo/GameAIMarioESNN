package ch.idsia.scenarios.test;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 6, 2009
 * Time: 8:21:23 PM
 */
public class PaperEvolveBatch
{

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            PaperEvolve.main(new String[]{"ch.idsia.evolution.agents.learning.SmallMLPAgent"});
            PaperEvolve.main(new String[]{"ch.idsia.evolution.agents.learning.SmallSRNAgent"});
            PaperEvolve.main(new String[]{"ch.idsia.evolution.agents.learning.MediumMLPAgent"});
            PaperEvolve.main(new String[]{"ch.idsia.evolution.agents.learning.MediumSRNAgent"});
            PaperEvolve.main(new String[]{"ch.idsia.evolution.agents.learning.LargeMLPAgent"});
            PaperEvolve.main(new String[]{"ch.idsia.evolution.agents.learning.LargeSRNAgent"});
        }
    }
}
