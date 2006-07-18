In this example, the input document will be unmarshalled a small chunk at 
a time, instead of unmarshalling the whole document at once. This reduces
the memory consumption and speeds up the turn around time.

The point of this example is to illustrate that JAXB allows the application
to intervene the unmarshalling process. This example can be used not only
for partial unmarshalling, but also for things like skipping a part of
a document or including other documents.
Writing an XMLFilter, as we did in this example, is one way to do this.
