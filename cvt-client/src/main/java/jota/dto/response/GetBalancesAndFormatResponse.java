package jota.dto.response;

import jota.model.Input;

import java.util.List;

/**
 * Response of api request 'getBalancesAndFormatResponse'.
 **/
public class GetBalancesAndFormatResponse extends AbstractResponse {

    private List<Input> inputs;
    private long totalBalance;

    /**
     * Initializes a new instance of the GetBalancesAndFormatResponse class.
     */
    public static GetBalancesAndFormatResponse create(List<Input> inputs, long totalBalance, long duration) {
        GetBalancesAndFormatResponse res = new GetBalancesAndFormatResponse();
        res.inputs = inputs;
        res.totalBalance = totalBalance;
        res.setDuration(duration);
        return res;
    }

    /**
     * Gets the input.
     *
     * @return The transactions.
     */
    public List<Input> getInputs() {
        return inputs;
    }


}
