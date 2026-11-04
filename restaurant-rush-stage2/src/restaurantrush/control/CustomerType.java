package restaurantrush.control;

import java.util.Random;

import restaurantrush.model.Customer;
import restaurantrush.model.CriticCustomer;
import restaurantrush.model.RegularCustomer;
import restaurantrush.model.VIPCustomer;

/**
 * The kinds of customer that can be scheduled to arrive. Each constant knows how
 * to create its own customer, so adding a type means adding a constant and a
 * Customer subclass, with no switch statement to update anywhere.
 */
public enum CustomerType {

    REGULAR("Regular") {
        @Override
        public Customer create(int id, int arrivalTurn, Random tasteRandom) {
            return new RegularCustomer(id, arrivalTurn, tasteRandom);
        }
    },
    VIP("VIP") {
        @Override
        public Customer create(int id, int arrivalTurn, Random tasteRandom) {
            return new VIPCustomer(id, arrivalTurn, tasteRandom);
        }
    },
    CRITIC("Critic") {
        @Override
        public Customer create(int id, int arrivalTurn, Random tasteRandom) {
            return new CriticCustomer(id, arrivalTurn, tasteRandom);
        }
    };

    private final String label;

    CustomerType(String label) {
        this.label = label;
    }

    public abstract Customer create(int id, int arrivalTurn, Random tasteRandom);

    public String getLabel() {
        return label;
    }
}
