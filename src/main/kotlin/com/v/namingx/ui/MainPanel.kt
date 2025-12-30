package com.v.namingx.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.v.namingx.AppSettings
import com.v.namingx.AppSettingsConfigurable
import com.v.namingx.service.GeneratorService
import com.v.namingx.service.Suggestion
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

class MainPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val descriptionInput = JBTextArea()
    private val resultPanel = JPanel()
    private val statusLabel = JLabel("坐等起名...", AllIcons.General.BalloonInformation, SwingConstants.LEFT)
    private val generateBtn = JButton("整一个！", AllIcons.Actions.Execute)
    private val settingsBtn = JButton(AllIcons.General.Settings)
    private val conventionBox =
        ComboBox(arrayOf("camelCase", "PascalCase", "snake_case", "kebab-case", "SCREAMING_SNAKE_CASE"))

    private val defaultInputs = listOf(
        "这个字段名字来自三次重构失败后的妥协",
        "明明能删但删了就会出事的代码块",
        "为了以后好维护而现在谁也看不懂的变量",
        "产品说先这样以后再说的那个参数",
        "理论上永远不会为 null 但我还是判断了",
        "写的时候很自信现在完全不敢动的逻辑",
        "用来安抚测试同学情绪的占位返回值",
        "只在凌晨两点才能正确运行的判断条件",
        "用于区分自己人和外包的神秘前缀",
        "改动它会引发连锁反应的核心变量",
        "用来证明这段代码不是我写的注释",
        "写完之后立刻忘记用途的辅助变量",
        "为了性能牺牲可读性的那一坨",
        "老板看了会点头但程序员会沉默的命名",
        "理论上可以复用但没人敢复用的实现",
        "专门用来应付演示环境的特殊开关",
        "这个布尔值同时代表三种相反含义",
        "看名字像常量但其实会变的东西",
        "为了让代码看起来有架构感而存在的层",
        "测试环境能跑线上直接报警的逻辑",
        "写给未来自己的留言但未来自己不看",
        "这个函数返回什么全靠上下文理解",
        "为了避免解释而取的更复杂的名字",
        "改需求时唯一不用改的那一行",
        "代码评审时大家默契跳过的部分",
        "用来延缓项目崩溃速度的临时方案",
        "历史包袱具象化之后的变量形态",
        "写的时候以为很优雅现在只剩回忆",
        "为了防止自己被骂而多写的一层判断",
        "这个值不重要但没有它就会出 bug",
        "名字很大实际作用很小的变量",
        "为了对齐接口文档而硬凑出来的字段",
        "只有创建者本人知道怎么用的参数",
        "复制粘贴时顺手留下的时代印记",
        "代码跑起来了但良心有点痛的实现",
        "为了少写一句话而多写十行代码",
        "每次调试都要从这里开始怀疑人生",
        "看似多余实则谁也不敢删的存在",
        "写完这行代码我就去思考人生",
        "命名的时候已经预感到以后会后悔"
    )

    private val loadingMessages = listOf(
        "正在祭拜图灵祖师爷...",
        "正在翻阅《代码大全》...",
        "给 CPU 喂口热饭，稍等...",
        "正在咨询代码之神...",
        "嘘... AI 正在冥想...",
        "正在与 bug 协商变量名...",
        "正在试图理解你的意图...",
        "正在加载... (假装很快)...",
        "正在连线火星服务器...",
        "正在向 Stack Overflow 祈祷...",
        "正在暴力破解你的需求...",
        "正在从天书中寻找灵感...",
        "正在召唤变量之神...",
        "正在给 AI 喂咖啡...",
        "正在查询《命名艺术》...",
        "正在解密你的脑洞...",
        "正在编译你的想法...",
        "正在与产品经理 battle...",
        "正在试图不抛出 Exception...",
        "正在清理内存泄漏...",
        "正在假装思考...",
        "正在寻找那个丢失的分号...",
        "正在优化 O(n!) 算法...",
        "正在重构宇宙...",
        "正在等待量子涨落...",
        "正在给服务器降温...",
        "正在数羊...",
        "正在思考人生...",
        "正在试图通过图灵测试...",
        "正在生成... (进度 99%)...",
        "正在寻找最佳实践...",
        "正在避免命名冲突...",
        "正在查字典...",
        "正在问 Siri...",
        "正在问 ChatGPT...",
        "正在问 Copilot...",
        "正在掷骰子...",
        "正在夜观天象...",
        "正在掐指一算...",
        "正在画符...",
        "正在请教老司机...",
        "正在翻陈年老代码...",
        "正在试图看穿一切...",
        "正在加载 Loading 条...",
        "正在忽悠 CPU 干活...",
        "正在唤醒沉睡的代码...",
        "正在寻找第 1024 个灵感...",
        "正在躲避 GC 回收...",
        "正在进行脑暴...",
        "正在试图变得聪明...",
        "正在加载... 可能会很久...",
        "正在加载... 马上就好...",
        "正在加载... 真的...",
        "正在加载... 骗你是小狗...",
        "正在加载... 再等一秒...",
        "正在加载... 别急...",
        "正在加载...喝口水吧...",
        "正在加载...看看窗外...",
        "正在加载...保护视力...",
        "正在加载...颈椎还好吗...",
        "正在加载...记得提肛...",
        "正在加载...多喝热水...",
        "正在加载...少熬夜...",
        "正在加载...发际线挺住...",
        "正在加载...拒绝内卷...",
        "正在加载...这需求太难了...",
        "正在加载...我想静静...",
        "正在加载...AI 也要休息...",
        "正在加载...服务器开小差了...",
        "正在加载...网线被挖断了？...",
        "正在加载...光纤被鲨鱼咬了？...",
        "正在加载...信号不好...",
        "正在加载...正在重试...",
        "正在加载...再试一次...",
        "正在加载...还没好...",
        "正在加载...快了快了...",
        "正在加载...这波稳了...",
        "正在加载...相信奇迹...",
        "正在加载...代码不仅是眼前的苟且...",
        "正在加载...还有诗和远方...",
        "正在加载...变量名不仅是代号...",
        "正在加载...更是灵魂...",
        "正在加载...起名见水平...",
        "正在加载...起名见人品...",
        "正在加载...起名好难...",
        "正在加载...不如叫 a, b, c...",
        "正在加载...不如叫 ch, i, j...",
        "正在加载...不如叫 temp, tmp, t...",
        "正在加载...不如叫 foo, bar, baz...",
        "正在加载...但是我们不一样...",
        "正在加载...我们要优雅...",
        "正在加载...我们要规范...",
        "正在加载...我们要高端...",
        "正在加载...我们要大气...",
        "正在加载...我们要上档次...",
        "正在加载...所以请稍等...",
        "正在加载...精彩即将呈现...",
        "正在加载...Ready?...",
        "正在加载...Go!..."
    )

    private val copyMessages = listOf(
        "这一行代码，是你改变世界的开始。",
        "复制成功，愿你的代码永无 Bug。",
        "这一刻，你不是一个人在战斗。",
        "这一生，代码与爱不可辜负。",
        "你的手速，守护着亿万用户的梦。",
        "每一次 CV，都是对效率的致敬。",
        "代码如诗，你就是最伟大的诗人。",
        "星辰大海，始于这行变量名。",
        "愿你出走半生，归来代码仍无 Bug。",
        "这一行，承载着尚未实现的梦想。",
        "复制的不是代码，是通往未来的钥匙。",
        "你要悄悄拔尖，然后惊艳所有人。",
        "即使是变量名，也要有它的骄傲。",
        "为了那个伟大的产品，可以！",
        "相信自己，你写的不是代码，是艺术。",
        "这一粘贴，bug 退散！",
        "优雅，永不过时。",
        "每一个字符，都闪耀着智慧的光芒。",
        "保持热爱，奔赴山海。",
        "心中有码，自然无 Bug。",
        "从这里开始，构建你的数字帝国。",
        "让代码更有温度，让世界更加美好。",
        "愿你的才华，配得上你的野心。",
        "与其感慨路难行，不如马上出发。",
        "这一行，是通往架构师的阶梯。",
        "不积跬步，无以至千里。",
        "拿走吧，这一行光荣与梦想！",
        "代码千万行，开心第一行。",
        "复制完成，你正在把混乱整理成秩序。",
        "这一行代码，配得上你认真对待的世界。",
        "你不只是写代码，你在表达一种理解。",
        "复制成功，安静但重要的一步。",
        "你正在构建的，不止是功能，还有意义。",
        "这一行，看似微小，却是你风格的一部分。",
        "复制完成，世界被你理解得更清楚了一点。",
        "你写得很慢，但每一步都算数。",
        "这一行代码，体现了你的耐心。",
        "复制成功，你选择了深思熟虑而不是将就。",
        "你不是在堆代码，你在寻找正确的形状。",
        "这一刻，你和系统达成了微妙的共识。",
        "复制完成，理性与直觉正在协作。",
        "你不追求炫技，你追求对。",
        "这一行，藏着你对细节的尊重。",
        "复制成功，你正在为未来的自己铺路。",
        "你不需要快过所有人，只需要走在自己的节奏里。",
        "这一段逻辑，很像你——克制、完整、有边界。",
        "复制完成，复杂问题被温柔地对待了。",
        "你正在把想法变成可以被理解的东西。",
        "这一行代码，选择了清晰而非喧哗。",
        "复制成功，你没有辜负自己的标准。",
        "你在为系统注入秩序感。",
        "这一刻，专注就是你的超能力。",
        "复制完成，你正在构建一个更可靠的世界。",
        "你写的不是最快的方案，而是最稳的。",
        "这一行，体现了你对长期的偏爱。",
        "复制成功，你没有敷衍这一刻。",
        "你习惯多想一步，这正是你的价值。",
        "这一行代码，是你世界观的延伸。",
        "复制完成，混沌被轻轻推开了一点。",
        "你不是在应付任务，你在完成承诺。",
        "这一段实现，很有你的风格。",
        "复制成功，安静地前进。",
        "你愿意为正确多花一点时间。",
        "这一行，未来会感谢你。",
        "复制完成，你选择了对自己诚实。",
        "你正在把复杂留给机器，把清晰留给人。",
        "这一刻，你没有急着结束。",
        "复制成功，你允许事情慢慢变好。",
        "你写代码的方式，很温柔。",
        "这一行，承担了它该承担的部分。",
        "复制完成，你没有放弃思考。",
        "你在构建一个你愿意长期生活的系统。",
        "这一行代码，不吵，但很稳。",
        "复制成功，你正在做长期正确的事。",
        "你不需要被所有人理解，这段代码懂你。",
        "这一刻，你站在逻辑与理想的交汇处。",
        "复制完成，世界因你而更有结构感。"
    )

    private val greetings = listOf(
        // --- 第一章：拒绝烂名字 (The "Just Don't" Collection) ---
        "别再用 'a', 'b', 'c' 了，我们不是在做算术题。",
        "那个叫 'temp' 的变量，最后都成了永久的。",
        "用拼音命名的同学，放学别走。",
        "拒绝 'var1'，你的词汇量不止于此。",
        "叫 'data' 跟没起名字有什么区别？",
        "不要用 'obj'，求求了，哪怕叫 'thing' 都好点。",
        "Flag 立得好，Bug 少不了。但变量名别叫 'flag'。",
        "拼写错误是万恶之源，让我来帮你拼对。",
        "如果你用了 'info'，我就死给你看。",
        "那是 'l' (小写L) 还是 '1' (数字1)？别折磨我。",
        "匈牙利命名法？现在是 21 世纪了，朋友。",
        "名字越短，Bug 藏得越深。",
        "全大写是常量的特权，变量请坐下。",
        "下划线还是驼峰？这是个问题，但别混着用。",
        "别把变量名写成小说，精简点！",
        "'foo' 和 'bar' 是用来测试的，不是用来上线的。",
        "你的变量名里怎么有数字？重构警告！",
        "看到 'util' 我就头疼，真的。",
        "别用 'manager'，它管的事儿太多了。",
        "你是想叫 'result' 还是 'ret'？统一一下好吗？",

        // --- 第二章：直击痛点 (The Pain is Real) ---
        "计算机科学两大难题：缓存失效，和给这货起名。",
        "起名两小时，代码五分钟。",
        "正在翻阅《牛津高阶词典》...",
        "为了这个变量名，头发又掉了两根。",
        "在这个名字想出来之前，不许去尿尿。",
        "这是上帝类 (God Class) 的前兆，控制住你自己。",
        "词穷了吗？这种感觉我懂。",
        "你的变量名，决定了你代码的颜值。",
        "别让未来的你，看着这行代码骂娘。",
        "起个好名字，就像给代码喷了香水。",
        "代码写得烂没关系，名字得起得专业点。",
        "命名不规范，同事两行泪。",
        "正在把你的 '意念' 编译成英文...",
        "如果名字太长，说明你的函数管得太宽。",
        "别解释，把逻辑写在名字里。",
        "好的变量名不需要注释。",
        "为了想这个名字，咖啡都凉了。",
        "键盘敲烂，名字难算。",
        "这代码是你写的，但名字是我起的，功劳算谁的？",
        "正在试图理解你的脑回路...",

        // --- 第三章：乔布斯的哲学 (The Steve Jobs Way) ---
        "Make it simple. Make it memorable.",
        "Stay hungry, stay foolish. 但名字要 Smart。",
        "这个变量名必须 Insanely Great。",
        "我们不是在写代码，我们在创造艺术。",
        "Simplicity is the ultimate sophistication.",
        "Think different. Name better.",
        "Details matter. 哪怕是一个变量名。",
        "Design is how it works. 名字就是它的设计。",
        "你想要把余生都浪费在改 Bug 上，还是想改变世界？",
        "One more name...",
        "这是我们做过最好的变量名，没有之一。",
        "它不仅仅是个变量，它是一种体验。",
        "去繁就简，直抵本质。",
        "哪怕是用户看不见的地方，也要完美。",
        "代码的背面也要打磨光滑。",
        "不要妥协，再想一个更好的。",
        "把平庸的名字丢进垃圾桶。",
        "你的代码应该像散文一样优美。",
        "这里的每一个字符都经过了精心设计。",
        "这就是现实扭曲力场：烂代码变好代码。",

        // --- 第四章：我是你的僚机 (Your Coding Wingman) ---
        "别慌，有我在，名字马上来。",
        "我是你的第二大脑，专门负责英语。",
        "把逻辑交给你，词汇量交给我。",
        "这就给你整一个看起来年薪百万的名字。",
        "我是你的变量名外挂，不算作弊吧？",
        "正在连接 GitHub 最强大脑...",
        "这个名字，只有你能驾驭。",
        "让你的代码看起来像出自大牛之手。",
        "我是你的救星，不用谢。",
        "专治各种起名困难综合症。",
        "这里有一个完美的变量名，请查收。",
        "不仅要跑得通，还要读得顺。",
        "我是代码界的莎士比亚。",
        "正在为您的代码注入灵魂...",
        "老板看不懂你的代码，但他能看懂这个名字。",
        "让 Code Review 变成一种享受。",
        "你的键盘侠上线了！",
        "帮你省下查谷歌翻译的时间。",
        "这个名字，产品经理看了都说好。",
        "我是你的优乐美... 啊不，我是你的命名器。",

        // --- 第五章：程序员的黑色幽默 (Nerd Humor) ---
        "面向变量名编程 (Variable-Oriented Programming)。",
        "玄学：名字起得好，Bug 居然变少了。",
        "正在祭天... 名字生成中。",
        "这个变量名我也没见过，看起来很厉害怕。",
        "StackOverflow 说这个名字可以用。",
        "Copilot 都要以此为榜样。",
        "这个名字价值一个 Star。",
        "为了防止世界被破坏，我推荐这个名字。",
        "既然你诚心诚意地发问了，那我就告诉你这个名字。",
        "真相只有一个，名字也只有一个！",
        "此处应有掌声（为这个变量名）。",
        "我在 3000 行屎山中，看见了光。",
        "如果代码能跑，就别动这个名字了。",
        "这个名字通过了图灵测试。",
        "正在咨询 GPT-4... 算了，还是我来吧。",
        "这是个 Feature，不是 Bug。",
        "PHP 是最好的... 算了，先起名吧。",
        "删库跑路前，先把变量名改好。",
        "不要回答！不要回答！不要回答！（我是指烂名字）",
        "愿源码与你同在。"
    )

    init {
        // Main Panel - No global padding to allow settings button to reach edges
        border = JBUI.Borders.empty()
        // Reverted to default background
        // background = UIUtil.getPanelBackground()

        // --- Top Bar (Greeting + Settings) ---
        val topBar = JPanel(BorderLayout())
        topBar.isOpaque = false

        // Greeting
        val greetingLabel = JLabel(greetings.random())
        greetingLabel.font = JBUI.Fonts.label(16f).asBold()
        greetingLabel.border = JBUI.Borders.empty(15, 15, 10, 0) // Top, Left, Bottom, Right
        topBar.add(greetingLabel, BorderLayout.WEST)

        // Settings Button
        settingsBtn.isBorderPainted = false
        settingsBtn.isContentAreaFilled = false
        settingsBtn.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        // Custom closer padding to the right edge
        settingsBtn.border = JBUI.Borders.empty(10, 10, 10, 0)
        settingsBtn.addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, AppSettingsConfigurable::class.java)
            updateConfigStatus()
        }
        updateConfigStatus()

        // Container
        val settingsContainer = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0))
        settingsContainer.isOpaque = false

        settingsContainer.add(settingsBtn, BorderLayout.NORTH)
        topBar.add(settingsContainer, BorderLayout.EAST)

        add(topBar, BorderLayout.NORTH)


        // --- content Body (Input + Results) ---
        val contentBody = JPanel(BorderLayout())
        contentBody.border = JBUI.Borders.empty(0, 15, 15, 15) // Left, Bottom, Right padding

        // Input Area - Default Style
        descriptionInput.rows = 3
        descriptionInput.lineWrap = true
        descriptionInput.wrapStyleWord = true
        descriptionInput.font = JBUI.Fonts.label(16f)
        refreshPlaceholder()
        descriptionInput.border = JBUI.Borders.empty(5, 5, 5, 5) // Normal padding
        // descriptionInput.background = UIUtil.getTextFieldBackground() // Let L&F handle it

        // Naming Convention Selector
        conventionBox.selectedItem = AppSettings.instance.namingConvention
        conventionBox.addActionListener {
            AppSettings.instance.namingConvention = conventionBox.selectedItem as String
        }

        // Key Listener
        descriptionInput.addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    if (e.isShiftDown) {
                        // Allow default behavior (newline)
                    } else {
                        e.consume() // Prevent newline
                        generateNames()
                    }
                }
            }
        })

        descriptionInput.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = checkEmpty()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = checkEmpty()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = checkEmpty()

            private fun checkEmpty() {
                if (descriptionInput.text.isEmpty()) {
                    SwingUtilities.invokeLater { refreshPlaceholder() }
                }
            }
        })

        val inputScrollPane = JBScrollPane(descriptionInput)
        // Default borders for scroll pane

        // Layered Pane logic removed as button is moved to top bar


        // Generate Button & Convention Layout
        // val generateBtn = JButton("Generate", AllIcons.Actions.Execute) // Now a class property
        generateBtn.addActionListener { generateNames() }

        val actionPanel = JPanel(BorderLayout())
        actionPanel.add(conventionBox, BorderLayout.WEST)
        actionPanel.add(generateBtn, BorderLayout.CENTER)

        // Auto-refresh on enter (when panel becomes visible)
        addAncestorListener(object : AncestorListener {
            override fun ancestorAdded(event: AncestorEvent?) {
                if (descriptionInput.text.isEmpty()) {
                    refreshPlaceholder()
                }
            }

            override fun ancestorRemoved(event: AncestorEvent?) {}
            override fun ancestorMoved(event: AncestorEvent?) {}
        })

        // Input Layout
        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(inputScrollPane, BorderLayout.CENTER)
        inputPanel.add(actionPanel, BorderLayout.SOUTH)

        // Add some spacing between input and results
        inputPanel.border = JBUI.Borders.emptyBottom(10)

        // Result Area
        resultPanel.layout = BoxLayout(resultPanel, BoxLayout.Y_AXIS)
        // Default opacity

        // Status Panel (Bottom) - Made more visible
        statusLabel.foreground = JBColor.GRAY
        statusLabel.font = JBUI.Fonts.label(12f)
        val statusPanel = JPanel(BorderLayout())
        statusPanel.border = JBUI.Borders.emptyTop(5)
        statusPanel.add(statusLabel, BorderLayout.CENTER)

        val scrollPane = JBScrollPane(resultPanel)
        scrollPane.border = JBUI.Borders.empty() // Clean look

        contentBody.add(inputPanel, BorderLayout.NORTH)
        contentBody.add(scrollPane, BorderLayout.CENTER)
        contentBody.add(statusPanel, BorderLayout.SOUTH)

        add(contentBody, BorderLayout.CENTER)
    }

    private fun refreshPlaceholder() {
        descriptionInput.emptyText.text = "示例：${defaultInputs.random()}"
    }

    private fun updateConfigStatus() {
        val apiKey = AppSettings.instance.apiKey
        if (apiKey.isBlank()) {
            settingsBtn.icon = AllIcons.General.Warning
            settingsBtn.toolTipText = "未配置 API Key，点击去配置"
        } else {
            settingsBtn.icon = AllIcons.General.GearPlain
            settingsBtn.toolTipText = "设置"
        }
    }

    private fun generateNames() {
        val text = descriptionInput.text.trim()
        if (text.isEmpty()) return

        setLoading(true)
        resultPanel.removeAll()
        resultPanel.revalidate()
        resultPanel.repaint()

        val service = GeneratorService()

        service.generateVariables(
            text,
            onSuccess = { suggestions ->
                SwingUtilities.invokeLater {
                    setLoading(false)
                    showResults(suggestions)
                }
            },
            onError = { errorMsg ->
                SwingUtilities.invokeLater {
                    setLoading(false)
                    showError(errorMsg)
                }
            }
        )
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            val msg = loadingMessages.random()
            statusLabel.text = msg
            statusLabel.icon = AllIcons.Actions.Refresh
            generateBtn.text = msg
            generateBtn.isEnabled = false
            conventionBox.isEnabled = false
        } else {
            statusLabel.text = "搞定！收工！"
            statusLabel.icon = AllIcons.General.BalloonInformation
            generateBtn.text = "整一个！"
            generateBtn.isEnabled = true
            conventionBox.isEnabled = true
        }
        descriptionInput.isEnabled = !isLoading
    }

    private fun showError(msg: String) {
        statusLabel.text = "哎呀，翻车了: $msg"
        statusLabel.icon = AllIcons.General.Error
        statusLabel.foreground = JBColor.RED
    }

    fun showResults(suggestions: List<Suggestion>) {
        resultPanel.removeAll()

        if (suggestions.isEmpty()) {
            val errLabel = JLabel("没词儿了，换个姿势试试？")
            errLabel.foreground = JBColor.GRAY
            resultPanel.add(errLabel)
        } else {
            for (suggestion in suggestions) {
                val card = createResultCard(suggestion)
                resultPanel.add(card)
                resultPanel.add(Box.createVerticalStrut(10)) // Spacing
            }
        }

        resultPanel.revalidate()
        resultPanel.repaint()
    }

    private fun createResultCard(suggestion: Suggestion): JPanel {
        val card = JPanel(BorderLayout())
        // Default border style instead of custom rounded
        card.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            JBUI.Borders.empty(10)
        )
        card.background = UIUtil.getEditorPaneBackground()
        card.maximumSize = Dimension(Int.MAX_VALUE, 80) // Taller for explanation

        val nameLabel = JLabel(suggestion.name)
        nameLabel.font = Font("JetBrains Mono", Font.BOLD, 14)
        nameLabel.foreground = JBColor.foreground()

        val explanationLabel = JLabel("<html><i>${suggestion.explanation}</i></html>")
        explanationLabel.foreground = JBColor.GRAY
        explanationLabel.font = JBUI.Fonts.label(12f)

        val textPanel = JPanel(BorderLayout())
        textPanel.isOpaque = false // Transparent to let card bg show
        textPanel.add(nameLabel, BorderLayout.NORTH)
        textPanel.add(explanationLabel, BorderLayout.CENTER)

        val copyBtn = JButton(AllIcons.Actions.Copy)

        val copyAction = {
            CopyPasteManager.getInstance().setContents(StringSelection(suggestion.name))

            // Icon swap animation
            val originalIcon = copyBtn.icon
            copyBtn.icon = AllIcons.Actions.Checked

            // Popup Feedback
            val msg = copyMessages.random()
            com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(msg, com.intellij.openapi.ui.MessageType.INFO, null)
                .setFadeoutTime(3000)
                .createBalloon()
                .show(
                    com.intellij.ui.awt.RelativePoint.getSouthOf(copyBtn),
                    com.intellij.openapi.ui.popup.Balloon.Position.below
                )

            // Revert icon after delay
            Timer(1000) {
                copyBtn.icon = originalIcon
            }.start()
        }

        copyBtn.isBorderPainted = false
        copyBtn.isContentAreaFilled = false
        copyBtn.toolTipText = "点击复制 - CV 大法好"
        copyBtn.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        copyBtn.addActionListener { copyAction() }

        card.add(textPanel, BorderLayout.CENTER)
        card.add(copyBtn, BorderLayout.EAST)

        // Wrapper for hover effect
        card.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                card.background = UIUtil.getListSelectionBackground(true)
            }

            override fun mouseExited(e: MouseEvent?) {
                card.background = UIUtil.getEditorPaneBackground()
            }

            override fun mouseClicked(e: MouseEvent?) {
                if (e != null && e.clickCount == 1) {
                    copyAction()
                }
            }
        })

        return card
    }
}
