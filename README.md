# Command Line Debugging Tutorial

So here, I take a side trip of the usual open-source software and talk a little bit about how to use the command line debugger in java.

The command is `jdb` and, in order to give you an idea of what it can do, I'll start out by giving you an example of code that I debugged (or even just reverse-engineered, if it happens to already be bug-free) and some of the jdb commands I used to step through it.

---

## List of JDB Commands Covered

| Command              | Meaning                                                      |
|----------------------|--------------------------------------------------------------|
| `list`               | `Shows nearby source lines`                                  |
| `locals`             | `Shows local variables`                                      |
| `print` <expression> | `Evaluate and display the expression`                        |
| `where`              | `Show the current stack trace`                               |
| `next`               | `Execute the next line without entering the called methods`  |
| `step`               | `Execute the next line, entering the called methods`         |
| `cont`               | `Continue until the next breakpoint or program termination.` |

Entries are saved as a simple JSON file in your home directory.

## A Live Debugging Session

To demonstrate usage of this tool, I created an example of a fairly complex program which can be used to execute either modular exponentiation or modular multiplication.  It is a rather tricky implementation because it not only prevents intermediate results from getting bigger than computer memory can generally handle, but, in the case of java long primitive values, prevents overflowing the maximum size of the long via modular multiplication.

### Pre-Requisite Establishing Expected Answer

We start out with a hand calculation of 5 * 7 modulo 31

![The Debugging Session Should Match This](hand_calculation.png)

So, a run of the program should produce debugging outputs which match the calculation.

### Actual Live Debugging Session

So, in my linux terminal, I started off with the following to compile the application.

`me@my-computerName:~/projects/command_line_debugging_example$ javac -g com/modulo/MainClass.java`

The -g allowed me to see the values of the local variables with `locals` commands.  This is what it produced:

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass 5 7 31 multiply
Initializing jdb ...
> stop at com.modulo.multiplication.ModuloMultiplier:23
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:23.
It will be set after the class is loaded.
> stop at com.modulo.multiplication.ModuloMultiplier:33
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:33.
It will be set after the class is loaded.
> stop at com.modulo.multiplication.ModuloMultiplier:35
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:35.
It will be set after the class is loaded.
> run
run com.modulo.MainClass 5 7 31 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:35
Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:33
Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:23

Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=23 bci=6
23                return 0L;

main[1] locals
Method arguments:
a = 5
b = 0
Local variables:
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;

main[1] locals
Method arguments:
a = 5
b = 1
Local variables:
x = 0
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;

main[1] locals
Method arguments:
a = 5
b = 2
Local variables:
x = 5
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;

main[1] locals
Method arguments:
a = 5
b = 3
Local variables:
x = 10
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;

main[1] locals
Method arguments:
a = 15
b = 6
Local variables:
x = 15
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=35 bci=63
35            return a - (l - x);

main[1] locals
Method arguments:
a = 5
b = 7
Local variables:
x = 30
main[1] print l
 l = 31
main[1] cont
> 4

The application exited
```

Believe it or not, this is actually showing the return values being assigned to the x values.  To see why I'm saying this, here is the same session again.  But this time, where I have annotated the x assignments with markers correlating them to the previous return values (jdb doesn't offer this kind of annotation, BTW, I just put it in there for clarity):

```
me@my-computerName:~/projects/command_line_debugging_example$ javac -g com/modulo/MainClass.java
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass 5 7 31 multiply
Initializing jdb ...
> stop at com.modulo.multiplication.ModuloMultiplier:23
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:23.
It will be set after the class is loaded.
> stop at com.modulo.multiplication.ModuloMultiplier:33
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:33.
It will be set after the class is loaded.
> stop at com.modulo.multiplication.ModuloMultiplier:35
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:35.
It will be set after the class is loaded.
> run
run com.modulo.MainClass 5 7 31 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:35
Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:33
Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:23

Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=23 bci=6
23                return 0L;  ********************** A

main[1] locals
Method arguments:
a = 5
b = 0
Local variables:
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;  ********************** B

main[1] locals
Method arguments:
a = 5
b = 1
Local variables:
x = 0  ********************** A
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;  ********************** C

main[1] locals
Method arguments:
a = 5
b = 2
Local variables:
x = 5  ********************** B
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;  ********************** D

main[1] locals
Method arguments:
a = 5
b = 3
Local variables:
x = 10  ********************** C
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=33 bci=58
33                return a + x;  ********************** E

main[1] locals
Method arguments:
a = 15  ~~~~~~~~~~~~~~~~~~~~~~ The "a" variable needed to be mutated, in this case, to continue working toward the correct answer.
b = 6
Local variables:
x = 15  ********************** D
main[1] cont
> 
Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=35 bci=63
35            return a - (l - x);  ********************** F

main[1] locals
Method arguments:
a = 5
b = 7
Local variables:
x = 30  ********************** E -- And, in this case, the calculation came from the mutated "a" variable at point E, not the original one.
main[1] print l
 l = 31
main[1] cont
> 4  ********************** F

The application exited
```

So, you can see that, at point A, x is being assigned 0 because the return statement returns 0.  At point B, x is being assigned 5 because the `return a+x;` had a = 5 and x = 0 at the time it was doing that and, if we keep going through this, the entire sequence of x values or return values is 0, 5, 10, 15, 30, 4 just like in the hand drawing.

#### A Closer Look

So, if we look more closely, when the application was prompting me with:

```
main[1] locals
Method arguments:
a = 5
b = 0
Local variables:
main[1] cont
```

The prompt I was getting was `main[1]`.  When I entered the `locals` command, that's when it printed the contents of the `a` and `b` variables.  Then, when I entered `cont` it continued until it hit the next breakpoint.

Then, when I executed:

```
main[1] print l
 l = 31
```

it printed the modulus, 31, which was the instance variable value, not one of the local variables.

## Other Commands Supported

So far, we have seen examples of `locals`, `cont` and `print`.  There are also `list`, `where`, `next` and `step` which we'll see examples of in this section.

### The `where` Command

This shows the stack trace.

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass 13 5 7 multiply
Initializing jdb ...
> stop at com.modulo.multiplication.ModuloMultiplier:23
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:23.
It will be set after the class is loaded.
> run
run com.modulo.MainClass 13 5 7 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:23

Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=23 bci=6
23                return 0L;

main[1] where
  [1] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:23)
  [2] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:27)
  [3] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:29)
  [4] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:29)
  [5] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:27)
  [6] com.modulo.multiplication.ModuloMultiplier.multiply (ModuloMultiplier.java:19)
  [7] com.modulo.MainClass.main (MainClass.java:16)
main[1] cont
> 2

The application exited
```

Here, I'll annotate it so it makes more visual sense:

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass 13 5 7 multiply
Initializing jdb ...
> stop at com.modulo.multiplication.ModuloMultiplier:23
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:23.
It will be set after the class is loaded.
> run
run com.modulo.MainClass 13 5 7 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:23

Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.recursiveMultiply(), line=23 bci=6
23                return 0L;

main[1] where
  [1] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:23)  ********************** Multiplying by 0
  [2] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:27)  ********************** Multiplying by 1
  [3] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:29)  ********************** Multiplying by 2
  [4] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:29)  ********************** Multiplying by 4
  [5] com.modulo.multiplication.ModuloMultiplier.recursiveMultiply (ModuloMultiplier.java:27)  ********************** Multiplying by 5
  [6] com.modulo.multiplication.ModuloMultiplier.multiply (ModuloMultiplier.java:19)
  [7] com.modulo.MainClass.main (MainClass.java:16)
main[1] cont
> 2

The application exited
```

It has exactly 5 recursive calls because of the reasoning that 5 is odd so subtract 1 and then 4 is even so divide by 2 then 2 is even so divide by 2 then 1 is odd so subtract 1 then 0 is the recursive base case.

### The `next` and `step` Commands

In a nutshell, these are the commands that stop execution at a particular line even though you didn't set a breakpoint there.  And then you have to remember which one you want or you'll either be stepping into something you wanted to skip over or skipping over something you wanted to step into.  They're presented here for completeness.  But, in my opinion, this has never been a good feature of debuggers.  Regardless of if they're the visual ones in the IDE or the command line ones.

Here's a `step` example:

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass 13 5 7 multiply
Initializing jdb ...
> stop at com.modulo.MainClass:8                               
Deferring breakpoint com.modulo.MainClass:8.
It will be set after the class is loaded.
> run
run com.modulo.MainClass 13 5 7 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.MainClass:8

Breakpoint hit: "thread=main", com.modulo.MainClass.main(), line=8 bci=0
8            long a = Long.parseLong(args[0]);

main[1] step
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=9 bci=7
9            long b = Long.parseLong(args[1]);

main[1] step
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=10 bci=14
10            long n = Long.parseLong(args[2]);

main[1] step
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=11 bci=22
11            if("power".equals(args[3])) {

main[1] step
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=14 bci=60
14            } else if("multiply".equals(args[3])){

main[1] step
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=15 bci=71
15                ModuloMultiplier mmult = new ModuloMultiplier(n);

main[1] step
> 
Step completed: "thread=main", com.modulo.multiplication.ModuloMultiplier.<init>(), line=6 bci=0
6        public ModuloMultiplier(long q) {

main[1] step
> 
Step completed: "thread=main", com.modulo.multiplication.ModuloMultiplier.<init>(), line=7 bci=4
7            l = q;

main[1] step
> 
Step completed: "thread=main", com.modulo.multiplication.ModuloMultiplier.<init>(), line=8 bci=9
8        }

main[1] cont
> 2

The application exited
```

As you can see from that example, I keep stepping and it starts to go into the logic for the ModuloMultiplier.  You can imagine that if that were a session where I wanted to skip past that to the point where it is executing the System.out.println of the result, it would be theoretically rather frustrating if I didn't remember that the command that I want is `next` instead of `step`.

And here's a `next` example:

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass 13 5 7 multiply
Initializing jdb ...
> stop at com.modulo.MainClass:8
Deferring breakpoint com.modulo.MainClass:8.
It will be set after the class is loaded.
> run
run com.modulo.MainClass 13 5 7 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.MainClass:8

Breakpoint hit: "thread=main", com.modulo.MainClass.main(), line=8 bci=0
8            long a = Long.parseLong(args[0]);

main[1] next
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=9 bci=7
9            long b = Long.parseLong(args[1]);

main[1] next
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=10 bci=14
10            long n = Long.parseLong(args[2]);

main[1] next
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=11 bci=22
11            if("power".equals(args[3])) {

main[1] next
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=14 bci=60
14            } else if("multiply".equals(args[3])){

main[1] next
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=15 bci=71
15                ModuloMultiplier mmult = new ModuloMultiplier(n);

main[1] next
> 
Step completed: "thread=main", com.modulo.MainClass.main(), line=16 bci=82
16                System.out.println(mmult.multiply(a, b));

main[1] next
> 2

Step completed: "thread=main", com.modulo.MainClass.main(), line=18 bci=95
18        }

main[1] cont
> 
The application exited
```

Looks much better, right?  Not really because it has the opposite problem.  If this were a theoretical scenario (which hasn't always been theoretical for me) where I actually wanted it to step into the modular multiplication logic, I would have had to remember that it is the `step` command, not the `next`.  And that's the real conundrum.  At every step or next, you have to remember whether you want a step or a next.

And here's an example where, when it stops at a breakpoint, you set an additional one and just `cont` to it ... a much more controlled way of doing things:

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass -1 5 7 multiply
Initializing jdb ...
> stop at com.modulo.MainClass:8
Deferring breakpoint com.modulo.MainClass:8.
It will be set after the class is loaded.
> run
run com.modulo.MainClass -1 5 7 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.MainClass:8

Breakpoint hit: "thread=main", com.modulo.MainClass.main(), line=8 bci=0
8            long a = Long.parseLong(args[0]);

main[1] stop at com.modulo.multiplication.ModuloMultiplier:13 
Deferring breakpoint com.modulo.multiplication.ModuloMultiplier:13.
It will be set after the class is loaded.
main[1] cont
> Set deferred breakpoint com.modulo.multiplication.ModuloMultiplier:13

Breakpoint hit: "thread=main", com.modulo.multiplication.ModuloMultiplier.multiply(), line=13 bci=13
13                a += l;

main[1] cont
> 2

The application exited
```

Here, the debugging is much more intuitive.  I stop at a line, then realize that I need to stop somewhere else while I'm stopped there and just dynamically add another breakpoint for it and `cont` until it.  All the guesswork of "Do I `step` or do I `next` at this line?" is neatly eliminated!

### The `list` Command

Finally, to round it all out, here is an example of the `list` command.

```
me@my-computerName:~/projects/command_line_debugging_example$ jdb -classpath . -sourcepath . com.modulo.MainClass -1 5 7 multiply
Initializing jdb ...
> stop at com.modulo.MainClass:8
Deferring breakpoint com.modulo.MainClass:8.
It will be set after the class is loaded.
> run
run com.modulo.MainClass -1 5 7 multiply
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
> 
VM Started: Set deferred breakpoint com.modulo.MainClass:8

Breakpoint hit: "thread=main", com.modulo.MainClass.main(), line=8 bci=0
8            long a = Long.parseLong(args[0]);

main[1] list
4    import com.modulo.multiplication.ModuloMultiplier;
5    
6    public class MainClass {
7        public static void main(String[] args) {
8 =>         long a = Long.parseLong(args[0]);
9            long b = Long.parseLong(args[1]);
10            long n = Long.parseLong(args[2]);
11            if("power".equals(args[3])) {
12                ModuloExponentiator mex = new ModuloExponentiator(n);
13                System.out.println(mex.power(a, b));
main[1] cont
> 2

The application exited
```

This shows the lines of surrounding context.  Not bad!  I happened not to need it at the beginning of this session because I happened to have the source also open in a text editor and could just scroll the cursor to the stopped at line.

## An Exercise For The Reader

Add similar breakpoints in the ModuloExponentiator.java.  You'll want to follow along with a calculator while you're going through it, doing each of the modular multiplications and squarings in turn.

Also, check out the official documentation at https://docs.oracle.com/en/java/javase/21/docs/specs/man/jdb.html