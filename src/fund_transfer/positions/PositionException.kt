package fund_transfer.positions

import java.lang.RuntimeException

class PositionNotFoundException(msg: String?) : RuntimeException(msg)

class BlockedAccountException(msg: String?) : RuntimeException(msg)

class LockedAccountException(msg: String?) : RuntimeException(msg)

class InsufficientBalanceException(msg: String?) : RuntimeException(msg)

class BodyDeserializationException(msg: String?) : RuntimeException(msg)

class ClosedAccountException(msg: String?) : RuntimeException(msg)
