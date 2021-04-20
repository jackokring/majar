Class Names
=

Suggest (Key)(PassedTenseVerb) as extending class names or (key)Of/On(Object).
This makes the inheritance tree group by base class. This advice is ignored
in some cases when the adjective has an overriding usage effect. For example
in BulkStream the "Bulk" nature is perhaps more important as an organizing
principal. StreamOfBulk kind of implies many bulks and a class Bulk, which
is not the case. I call this an "adjective functional" where the Stream
nature is considered useful.

BulkStream implements Stream, Iterable
=

To support Iterator via yield() and allow mass data actions in parallel
via the Stream methods. Iterator methods too. The load into a Stream has
many possibilities. The Base then can add extra indexing information
and other extras, stripping non-essential secondary data from the save.
Indexes themselves may be stored in some kind of Key object.
