<blockquote>最近很多朋友都在问maplefetion的处理链设计模式，之前给一个网友详细的解答过，现在把我的回复贴在下面，希望对大家理解maplefetion里面的结构有所理解。</blockquote>

HI CJ.Yang:
> ProcessorChain是从设计模式中的职责链设计模式变化而来的，通过在这条双向的链表上传递一个对象，每个处理器可以进行处理或者替换后交给下一个处理器，也可以直接切断处理链。这样做是为了形成类似于堆栈似的协议处理，而且有明显的分层，每一层都完成独立的处理，下面是maplefetion的处理链的结构，或者叫处理栈更贴切些：

Dialog  //最顶层，发起某一个操作，比如添加好友，会构建一个SipcRequest传递给下一层，并且捕获所有的异常和处理
> ↓↑
MessageDispatcher  //路由从底层传递上来的SipcNotify和SipcResponse,调用NotifyHandler和ResponseHandler
> ↓↑
SipcLogger     //记录所有和服务器交互的Sipc信令，便于调试，仅在调试模式下有效
> ↓↑
TransferService // 管理Sipc信令的传输，处理超时和重传，以及SipcResponse和SipcRequest的配对
> ↓↑
SipcParser     //解析底层传递过来的字节数组为单独的Sipc信令，并交个上层处理，和把上层传递过来的Sipc信令转为字节数组，交给下层发送；
> ↓↑
Transfer    //发送和接受网络传递的字节数组

通过这种处理链的方式，可以很容易构建起分层的处理结构，并且可以很方便的在这个处理链上添加新处理器来添加新的功能，很容易维护。
缺点嘛，暂时没想到，呵呵。

如果你看过Tomcat的源码，其实里面的Valve也是一个处理链的结构，对于每一个请求都会在有Valve构成的链表中传递和处理，还有Filter也是。
希望对你有所帮助。

solosky