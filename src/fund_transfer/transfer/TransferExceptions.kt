package fund_transfer.transfer

import java.lang.RuntimeException

class SamePositionTransferException(msg: String?) : RuntimeException(msg)

class NegativeTransferAmountException(msg: String?) : RuntimeException(msg)
