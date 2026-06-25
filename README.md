# Command Line Debugging Tutorial

So here, I take a side trip of the usual open-source software and talk a little bit about how to use the command line debugger in java.

The command is `jdb` and, in order to give you an idea of what it can do, I'll start out by giving you an example of code that I debugged (or even just reverse-engineered, if it happens to already be bug-free) and some of the jdb commands I used to step through it.

---

## List of JDB Commands Covered

| Command                                           | Meaning                                                                    |
|---------------------------------------------------|----------------------------------------------------------------------------|
| `list`                                            | `Shows nearby source lines`                                                |
| `locals`                                          | `Shows local variables`                                                    |
| `print` <expression>                              | `Evaluate and display the expression`                                      |
| `where`                                           | `Show the current stack trace`                                             |
| `next`                                            | `Execute the next line without entering the called methods`                |
| `step`                                            | `Execute the next line, entering the called methods`                       |
| `cont`                                            | `Continue until the next breakpoint or program termination.`               |
| `stop at `<package>`.`<ClassName>`:`<line number> | `Actually sets a break point in the program at the specified line number.` |

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

Here, the debugging is much more intuitive.  I stop at a line, then realize that I need to stop in the negative normalization path also while I'm stopped where I currently am and just dynamically add another breakpoint for it and `cont` until it.  All the guesswork of "Do I `step` or do I `next` at this line?" is neatly eliminated!

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

## Debugging Unit Tests In Gradle

### Debugging Markers Appear To Be Enabled In A Gradle Build

Remember in the sections above when I said that the -g option gives you the ability to see the values of the local variables with `locals` commands when compiling just with javac?  Gradle (or, at least the gradlew wrapper 8.14.5) makes that irrelevant.  Instead, the default behavior appears to give full command line debugging capability out-of-the-box.  If you're curious to see what it looks like when certain capabilities are disabled, start by putting the following in your build.gradle as a template you can work from:

```
tasks.withType(JavaCompile).configureEach {
    options.debug = true
    options.debugOptions.debugLevel = "source,lines,vars"
}
```

which is the default behavior out-of-the-box anyway.  Then start taking away some of the abilities from the above and clean and build accordingly.

### Attaching A Debugger To A Gradle Run Of A Unit Test

Here, I'll show the example of attaching a gradle debugger to one of the unit tests in promptsanitizer, since that is another open-source project in one of my repos.  If you're debugging a test, you should probably focus on just one test.  So, I'll be demonstrating it with the `--tests argument`.  I started off by doing this:

`me@my-computerName:~/projects/promptsanitizer$ ./gradlew test --debug-jvm --tests 'promptsanitizer.batchjob.MergeUtilTest'`

However, one tricky thing about this is that, if you already ran the test, then it will be in Gradle's execution history.  So, in that case you'll also want the `--rerun` argument.

`me@my-computerName:~/projects/promptsanitizer$ ./gradlew test --rerun --debug-jvm --tests 'promptsanitizer.batchjob.MergeUtilTest'`

The output will be paused, and you'll actually have to open another terminal window to continue.  The output will look something like this:

```
> Task :test
Listening for transport dt_socket at address: 5005
```

Here, in this example, I opened another terminal window and attached a debugger with the command `jdb -sourcepath ./src/test/java:./src/main/java -attach localhost:5005`.  Then, I executed some of the commands we have seen in sections above just to demonstrate that it looks exactly the same when you're debugging a gradle run of a unit test.  Here is the complete breakdown of that:

```
me@my-computerName:~/projects/promptsanitizer$ jdb -sourcepath ./src/test/java:./src/main/java -attach localhost:5005
Set uncaught java.lang.Throwable
Set deferred uncaught java.lang.Throwable
Initializing jdb ...

VM Started: > No frames on the current call stack

main[1] stop at promptsanitizer.batchjob.MergeUtil:13
Deferring breakpoint promptsanitizer.batchjob.MergeUtil:13.
It will be set after the class is loaded.
main[1] run
> Set deferred breakpoint promptsanitizer.batchjob.MergeUtil:13

Breakpoint hit: "thread=Test worker", promptsanitizer.batchjob.MergeUtil.removeIfHas(), line=13 bci=14
13                return true;

Test worker[1] locals
Method arguments:
checkIfHasKey = instance of org.json.JSONObject(id=2797)
key = "key2"
Local variables:
Test worker[1] where
  [1] promptsanitizer.batchjob.MergeUtil.removeIfHas (MergeUtil.java:13)
  [2] promptsanitizer.batchjob.MergeUtilTest.removeIfHasShouldReturnTrueIfWasThereToRemove (MergeUtilTest.java:19)
  [3] java.lang.invoke.LambdaForm$DMH/0x00007ce188144000.invokeVirtual (null)
  [4] java.lang.invoke.LambdaForm$MH/0x00007ce18809c800.invoke (null)
  [5] java.lang.invoke.Invokers$Holder.invokeExact_MT (null)
  [6] jdk.internal.reflect.DirectMethodHandleAccessor.invokeImpl (DirectMethodHandleAccessor.java:153)
  [7] jdk.internal.reflect.DirectMethodHandleAccessor.invoke (DirectMethodHandleAccessor.java:103)
  [8] java.lang.reflect.Method.invoke (Method.java:580)
  [9] org.junit.platform.commons.util.ReflectionUtils.invokeMethod (ReflectionUtils.java:728)
  [10] org.junit.jupiter.engine.execution.MethodInvocation.proceed (MethodInvocation.java:60)
  [11] org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed (InvocationInterceptorChain.java:131)
  [12] org.junit.jupiter.engine.extension.TimeoutExtension.intercept (TimeoutExtension.java:156)
  [13] org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod (TimeoutExtension.java:147)
  [14] org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod (TimeoutExtension.java:86)
  [15] org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor$$Lambda/0x00007ce188110a78.apply (null)
  [16] org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0 (InterceptingExecutableInvoker.java:103)
  [17] org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall$$Lambda/0x00007ce188110e98.apply (null)
  [18] org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0 (InterceptingExecutableInvoker.java:93)
  [19] org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$$Lambda/0x00007ce18813b970.apply (null)
  [20] org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed (InvocationInterceptorChain.java:106)
  [21] org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed (InvocationInterceptorChain.java:64)
  [22] org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke (InvocationInterceptorChain.java:45)
  [23] org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke (InvocationInterceptorChain.java:37)
  [24] org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke (InterceptingExecutableInvoker.java:92)
  [25] org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke (InterceptingExecutableInvoker.java:86)
  [26] org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7 (TestMethodTestDescriptor.java:218)
  [27] org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor$$Lambda/0x00007ce188141c70.execute (null)
  [28] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [29] org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod (TestMethodTestDescriptor.java:214)
  [30] org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute (TestMethodTestDescriptor.java:139)
  [31] org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute (TestMethodTestDescriptor.java:69)
  [32] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6 (NodeTestTask.java:156)
  [33] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812dab8.execute (null)
  [34] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [35] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8 (NodeTestTask.java:146)
  [36] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812d890.invoke (null)
  [37] org.junit.platform.engine.support.hierarchical.Node.around (Node.java:137)
  [38] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9 (NodeTestTask.java:144)
  [39] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812d468.execute (null)
  [40] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [41] org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively (NodeTestTask.java:143)
  [42] org.junit.platform.engine.support.hierarchical.NodeTestTask.execute (NodeTestTask.java:100)
  [43] org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService$$Lambda/0x00007ce18812c400.accept (null)
  [44] java.util.ArrayList.forEach (ArrayList.java:1,596)
  [45] org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll (SameThreadHierarchicalTestExecutorService.java:41)
  [46] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6 (NodeTestTask.java:160)
  [47] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812dab8.execute (null)
  [48] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [49] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8 (NodeTestTask.java:146)
  [50] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812d890.invoke (null)
  [51] org.junit.platform.engine.support.hierarchical.Node.around (Node.java:137)
  [52] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9 (NodeTestTask.java:144)
  [53] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812d468.execute (null)
  [54] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [55] org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively (NodeTestTask.java:143)
  [56] org.junit.platform.engine.support.hierarchical.NodeTestTask.execute (NodeTestTask.java:100)
  [57] org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService$$Lambda/0x00007ce18812c400.accept (null)
  [58] java.util.ArrayList.forEach (ArrayList.java:1,596)
  [59] org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll (SameThreadHierarchicalTestExecutorService.java:41)
  [60] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6 (NodeTestTask.java:160)
  [61] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812dab8.execute (null)
  [62] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [63] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8 (NodeTestTask.java:146)
  [64] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812d890.invoke (null)
  [65] org.junit.platform.engine.support.hierarchical.Node.around (Node.java:137)
  [66] org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9 (NodeTestTask.java:144)
  [67] org.junit.platform.engine.support.hierarchical.NodeTestTask$$Lambda/0x00007ce18812d468.execute (null)
  [68] org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute (ThrowableCollector.java:73)
  [69] org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively (NodeTestTask.java:143)
  [70] org.junit.platform.engine.support.hierarchical.NodeTestTask.execute (NodeTestTask.java:100)
  [71] org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit (SameThreadHierarchicalTestExecutorService.java:35)
  [72] org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute (HierarchicalTestExecutor.java:57)
  [73] org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute (HierarchicalTestEngine.java:54)
  [74] org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute (EngineExecutionOrchestrator.java:198)
  [75] org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute (EngineExecutionOrchestrator.java:169)
  [76] org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute (EngineExecutionOrchestrator.java:93)
  [77] org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0 (EngineExecutionOrchestrator.java:58)
  [78] org.junit.platform.launcher.core.EngineExecutionOrchestrator$$Lambda/0x00007ce18811e9e8.accept (null)
  [79] org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams (EngineExecutionOrchestrator.java:141)
  [80] org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute (EngineExecutionOrchestrator.java:57)
  [81] org.junit.platform.launcher.core.DefaultLauncher.execute (DefaultLauncher.java:103)
  [82] org.junit.platform.launcher.core.DefaultLauncher.execute (DefaultLauncher.java:85)
  [83] org.junit.platform.launcher.core.DelegatingLauncher.execute (DelegatingLauncher.java:47)
  [84] org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestClassProcessor$CollectAllTestClassesExecutor.processAllTestClasses (JUnitPlatformTestClassProcessor.java:124)
  [85] org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestClassProcessor$CollectAllTestClassesExecutor.access$000 (JUnitPlatformTestClassProcessor.java:99)
  [86] org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestClassProcessor.stop (JUnitPlatformTestClassProcessor.java:94)
  [87] org.gradle.api.internal.tasks.testing.SuiteTestClassProcessor.stop (SuiteTestClassProcessor.java:63)
  [88] java.lang.invoke.LambdaForm$DMH/0x00007ce18809c000.invokeInterface (null)
  [89] java.lang.invoke.LambdaForm$MH/0x00007ce18809c800.invoke (null)
  [90] java.lang.invoke.Invokers$Holder.invokeExact_MT (null)
  [91] jdk.internal.reflect.DirectMethodHandleAccessor.invokeImpl (DirectMethodHandleAccessor.java:153)
  [92] jdk.internal.reflect.DirectMethodHandleAccessor.invoke (DirectMethodHandleAccessor.java:103)
  [93] java.lang.reflect.Method.invoke (Method.java:580)
  [94] org.gradle.internal.dispatch.ReflectionDispatch.dispatch (ReflectionDispatch.java:36)
  [95] org.gradle.internal.dispatch.ReflectionDispatch.dispatch (ReflectionDispatch.java:24)
  [96] org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch (ContextClassLoaderDispatch.java:33)
  [97] org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke (ProxyDispatchAdapter.java:92)
  [98] jdk.proxy1.$Proxy4.stop (null)
  [99] org.gradle.api.internal.tasks.testing.worker.TestWorker$3.run (TestWorker.java:200)
  [100] org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName (TestWorker.java:132)
  [101] org.gradle.api.internal.tasks.testing.worker.TestWorker.execute (TestWorker.java:103)
  [102] org.gradle.api.internal.tasks.testing.worker.TestWorker.execute (TestWorker.java:63)
  [103] org.gradle.process.internal.worker.child.ActionExecutionWorker.execute (ActionExecutionWorker.java:56)
  [104] org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call (SystemApplicationClassLoaderWorker.java:122)
  [105] org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call (SystemApplicationClassLoaderWorker.java:72)
  [106] worker.org.gradle.process.internal.worker.GradleWorkerMain.run (GradleWorkerMain.java:69)
  [107] worker.org.gradle.process.internal.worker.GradleWorkerMain.main (GradleWorkerMain.java:74)
Test worker[1] list
9    public class MergeUtil {
10        static boolean removeIfHas(JSONObject checkIfHasKey, String key) {
11            if(checkIfHasKey.has(key)) {
12                checkIfHasKey.remove(key);
13 =>             return true;
14            }
15            return false;
16        }
17        static boolean putIfNotHasOrDifferent(JSONObject checkIfHasKey, String key, Object putThisThere) {
18            if(checkIfHasKey.has(key)) {
Test worker[1] cont
> 
The application exited
me@my-computerName:~/projects/promptsanitizer$
```

And finally, back in the first terminal window, the output will complete with this:

```
BUILD SUCCESSFUL in 3m 1s
5 actionable tasks: 1 executed, 4 up-to-date
me@my-computerName:~/projects/promptsanitizer$ 
```

Just for completeness, the reason that the locals showed a method argument of `key` being equal to `"key2"` is because of the actual code in that unit test (at the time of this writing):

```
    @Test
    void removeIfHasShouldReturnTrueIfWasThereToRemove() {
        JSONObject jo = new JSONObject("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        assertTrue(MergeUtil.removeIfHas(jo, "key2"));
        assertEquals("value1", jo.getString("key1"));
        assertFalse(jo.has("key2"));
    }
```

This code can be found at https://github.com/jmburke1/promptsanitizer.  The point here, though, is to demonstrate attaching the command line debugger to an actual gradle spawned run.  Basically, it's a few extra gradle arguments and the attach argument to jdb.