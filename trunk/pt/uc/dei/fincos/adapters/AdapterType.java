package pt.uc.dei.fincos.adapters;

/**
 * The types of adapters supported by FINCoS.
 */
public enum AdapterType {

    /** Sends and receives events using client APIs of CEP engines. */
    CEP,

    /** Sends and receives events through JMS messages. */
    JMS
}
