package uk.co.kring.lang;

import java.util.ListResourceBundle;

/**
 * Translations of error messages.
 */
public class Errors_zh extends ListResourceBundle {

    /**
     * The error messages of the interpreter.
     */
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                { "0", "输入或输出问题。这个过程被打断了吗？好的" },           //0
                { "1", "堆栈下溢。没有为某个单词提供足够的数据" }, //1
                { "2", "内存不足。也许堆栈溢出了" },  //2
                { "3", "缺少\"。请检查您的代码" },      //3
                { "4", "外部处理错误" },//4
                { "5", "找不到单词。这个词没有定义并且在书中" },       //5
                { "6", "受保护的圣经。圣经中有保留字" },//6
                { "7", "培养你一个无可辩驳的。是的，圣经无法撤销，但可以扩展" },//7
                { "8", "糟糕的环境。有一个定义，但在上下文链中没有。使用上下文" },     //8
                { "9", "错误的插件。提供单词作为上下文插件的Java类不是扩展Prim的类" },  //9
                { "10", "不！你不能以这种方式改变圣经。考虑分叉和编辑Java Bible类构建方法" },     //10
                { "11", "带引号的字符串格式错误。请勿在单词中间使用\"并留空格" },   //11
                { "12", "没有名称的符号。符号必须具有名称才能将其写入书中" },   //12
                { "13", "覆盖的书。现在所有的话都消失了" },  //13
                { "14", "部分上下文已删除。上下文链中的某些书已不存在" },  //14
                { "15", "当前书已删除。当前的书已被设置成圣经" },  //15
                { "16", "多本图书已删除。大量删除书籍" },  //16
                { "17", "错误的执行上下文。这本书已被删除。只是外壳" },     //17
                { "18", "无法对此进行多线程处理。某些东西拒绝重复并提供每个线程唯一的存储" }, //18
                { "19", "宏终端过多。有些词必须在前面。像代码括号" }, //19
                { "20", "Nul终端问题。你疯了。不要假装东西还在堆" },   //20
                { "21", "混乱进行中。鉴于您喜欢使用nul，这里有 ..."}, //21

                { "abort", "用户中止的过程." },
        };
    }
}