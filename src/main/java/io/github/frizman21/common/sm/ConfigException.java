package io.github.frizman21.common.sm;

/**
 * This exception is used when your setup of the state machine is wrong.
 */
public class ConfigException extends Exception {

  private static final long serialVersionUID = 4551315925157420883L;

  ConfigException(String message) {
    super(message);
  }

  ConfigException(String message, Throwable cause) {
    super(message, cause);
  }

}
