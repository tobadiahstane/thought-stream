# Thought-Stream

This is an exercise in designing and implementing an application using the principals of clean architecture.

## On Thoughts:

A thought contains:

  A short text - the 'thought' being had, which cannot be changed once the thought is had.
  An optional link to a url as the subject of the thought which cannot be added removed or changed once the thought is had.
  the time of the thought in question

As the original details of a thought cannot be changed, thoughts can be 'refined' adding more thought text at a different time period.

Thoughts can be connected together to form thought-streams.


## On Streams:

A Stream contains:
  A name.
  Two or more thoughts which are bound together by the stream.
  A focus - currently as text - of the stream

The focus of a stream may change.

Connected thoughts may be disconnected from the stream.


## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
