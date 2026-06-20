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
