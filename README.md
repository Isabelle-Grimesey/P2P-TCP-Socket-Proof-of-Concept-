# P2P-TCP-Socket-Proof-of-Concept-
For Texas A&amp;M's class ECEN 424.

Run Instructions:

- Clone.
- Navigate into any of the 3 top level folders. (Note: the QNA P2P Implementation is still incomplete and may not run.)
- Run the following to compile all the files in the folder:
```javac <Filename>.java```
- Run the following to run the executable.
```java <Executable>```

Future Work:
- Handle race conditions in answer-forwarding.
- Implement distributed storage for the ServerClient version of the QNA app. That is, have the central server not store any Questions, but rather query the respective askers and forward the responses when sharing questions to other members of the forum.
- Implement a limit on the number of clients for the ServerClient verion of the QNA app.
- Design and implement a P2P version of the QNA app.