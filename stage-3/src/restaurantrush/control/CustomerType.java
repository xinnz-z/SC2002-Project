package restaurantrush.control;

import java.util.Random;

import restaurantrush.model.Customer;
import restaurantrush.model.CriticCustomer;
import restaurantrush.model.RegularCustomer;
import restaurantrush.model.VIPCustomer;

/**
 * The kinds of customer that can be scheduled to arrive. Each constant knows how
 * to create its own customer, so no switch on customer type is needed anywhere.
 */
public enum CustomerType {

    REGULAR("Regular") {
        @Override
        public Customer create(int id, Random tasteRandom) {
            return new RegularCustomer(id, tasteRandom);
        }
    },
    VIP("VIP") {
        @Override
        public Customer create(int id, Random tasteRandom) {
            return new VIPCustomer(id, tasteRandom);
        }
    },
    CRITIC("Critic") {
        @Override
        public Customer create(int id, Random tasteRandom) {
            return new CriticCustomer(id, tasteRandom);
        }
    };

    private final String label;

    CustomerType(String label) {
        this.label = label;
    }

    public abstract Customer create(int id, Random tasteRandom);

    public String getLabel() {
        return label;
    }
}
