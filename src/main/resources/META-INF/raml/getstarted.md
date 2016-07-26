# System Account

All users accounts are created with zero balance, after that they can be credited from System Account. **System Account** is the only one that you may credit directly. In this example there is no System Account, just set the field `from` of the transfer to `null` or omit it &mdash; that transfer is considered to come from System Account

## User Account

After being created user account can be credited from System Account (just omit the `from` in transfer) &mdash; it simulates transferring money from outside via System Account.
