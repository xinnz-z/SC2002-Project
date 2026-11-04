package restaurantrush.control;

import restaurantrush.model.GameStats;

/** The final verdict, with which targets were met so the view can explain it. */
public final class GameResult {

    private final Objective objective;
    private final GameStats stats;
    private final boolean paidTargetMet;
    private final boolean revenueTargetMet;
    private final boolean satisfactionTargetMet;

    GameResult(Objective objective, GameStats stats, boolean paidTargetMet,
               boolean revenueTargetMet, boolean satisfactionTargetMet) {
        this.objective = objective;
        this.stats = stats;
        this.paidTargetMet = paidTargetMet;
        this.revenueTargetMet = revenueTargetMet;
        this.satisfactionTargetMet = satisfactionTargetMet;
    }

    public GameOutcome getOutcome() {
        return (paidTargetMet && revenueTargetMet && satisfactionTargetMet) ? GameOutcome.VICTORY : GameOutcome.DEFEAT;
    }

    public Objective getObjective() {
        return objective;
    }

    public GameStats getStats() {
        return stats;
    }

    public boolean isPaidTargetMet() {
        return paidTargetMet;
    }

    public boolean isRevenueTargetMet() {
        return revenueTargetMet;
    }

    public boolean isSatisfactionTargetMet() {
        return satisfactionTargetMet;
    }
}
