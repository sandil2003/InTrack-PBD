**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**A S S I G N M E NT 01** FN 

**Year 3 - Semester 2 - 2026** 

## **Assignment 01** 

**Designing a Personal Finance Management System for Android** 

_A Problem-First, Scenario-Driven Engineering Challenge_ 

|**Module Code**|SE3092|
|---|---|
|**Module Title**|Platform Based Development|
|**Assignment Type**|**Group Assignment(Maximum 3 Members)**|
|**Contribution**|40% of Final Module Grade|
|**Issued**|23/09/2026|
|**Submission Deadline**|**30/10/2026**|
|**Submission Mode**|LMS Upload + Scheduled Live Demo & Presentation|
|**Minimum SDK**|API Level 26(Android 8.0)Target SDK 34+|
|**Technology Stack**|Kotlin  ·  Jetpack Compose  ·  MVVM  ·  Firebase|
|**Prepared By**|H.A.A.C Perera|



**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**Year 3 - Semester 2 - 2026** 

**A S S I G N M E NT 01** FN 

#### **<u><mark>1. Background The Industry Philosophy</mark></u>** 

In production software engineering, clean requirements documents are a luxury that rarely exists. Engineers at high-performing organizations are handed context user research, friction logs, stakeholder narratives and are expected to derive clarity from ambiguity before writing a single line of code. The systems that genuinely change how people live are built by engineers who understand the problem behind the problem. 

This assignment is an exercise in that discipline. Rather than a feature checklist, you are given a scenario a detailed, realistic account of a real person navigating a real financial problem. Your primary obligation is not implementation; it is comprehension and translation. You must read, analyses, model, and design before you open Android Studio. The quality of your thinking upstream will be more consequential than the elegance of any individual Composable. 

#### **<mark>2. User Persona</mark>** 

Before engaging with the scenario, study who Kavindu Silva is. This is not a fictional archetype invented for academic convenience. He is a composite drawn from documented interviews with junior engineers across the Sri Lankan technology industry. His frustrations are common. His failed attempts are representative. 

|**PERSONA PROFILE**|**Mr. Kavindu Silva — The Ambitious Earner Without a System**|
|---|---|
|**Full Name**|Kavindu Silva|
|**Age**|25years old|
|**Education**|BSc(Hons)Software Engineering—graduated 12 months ago|
|**Current Role**|Junior Software Engineer,product-focused startup, Colombo|
|**Monthly Salary**|LKR 120,000 – 140,000(variable: base +performance component)|
|**Side Income**|Freelance web projects (LKR 20,000–90,000/project). Google AdSense in<br>USD. Cryptocurrencytrading (USDT, ETH, altcoins)|
|**Financial Goal**|Purchase a MacBook Pro M4 approx. LKR 490,000.00 within 12 months|
|**Current Savings**|LKR 11,200.00 after 12 months of consistent earning|
|**Personality**|Driven and technically sharp. Impulsive with discretionary spending.<br>Optimistic about income. avoidant about expenses.|
|**Core Frustration**|Cannot explain where his money goes. Every tracking attempt has failed<br>within two weeks. Feels financiallyinvisible despite earningwell.|



#### **<u><mark>3.  The Scenario — Kavindu's Financial Reality</mark></u>** 

##### **<u>3.1 The Setup</u>** 

Kavindu Silva graduated 12 months ago and moved into his first full-time engineering role with the conviction that financial stability was finally within reach. The salary was transformative more consistent income than he had ever managed. He told himself he would save aggressively from month one. He did not. Twelve months later, with over LKR 1.6 million earned in aggregate, he 

**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**Year 3 - Semester 2 - 2026** 

**A S S I G N M E NT 01** a 

has LKR 11,200.00 in his savings account. He cannot articulate where the rest went. He has genuinely tried to reconstruct it and failed. 

##### **<u>3.2 The Income Problem</u>** 

Kavindu's income is structurally irregular, and this is central to his difficulty. He earns from four distinct channels, each operating on different cycles, in different currencies, and with different levels of predictability: 

- His monthly salary arrives on the 25th but fluctuates between LKR 120,000 and LKR 140,000 depending on performance bonuses. He rarely examines his payslip in detail and frequently cannot recall the exact amount deposited. 

- He accepts freelance engagements primarily React and WordPress builds for local SMEs. Payments range from LKR 20,000 to LKR 90,000 per project, are milestone-based, and arrive irregularly. He has twice forgotten to follow up on outstanding invoices and once could not reconcile which payment corresponded to which project. 

- His tech blog generates Google AdSense revenue paid monthly in USD, converted to LKR at the prevailing rate at the time of transfer. Some months yield the equivalent of LKR 3,500; others reach LKR 19,000. He checks the AdSense dashboard occasionally but never records what he actually receives. 

- He actively trades USDT, ETH, and several altcoins on Binance. He recorded a significant gain in Q1 and a material loss in Q3. He does not maintain a running net position. When asked what his total crypto earnings are year-to-date, he cannot answer. 

When Kavindu attempts to calculate his monthly income, he opens four separate applications, navigates to his bank statement, checks his email, and abandons the exercise before completing it. He estimates his monthly income at 'somewhere between LKR 160,000 and 210,000' but has never validated this figure. 

##### **<u>3.3  The Expense Invisibility</u>** 

Kavindu's expenditure is fragmented across channels he does not monitor in aggregate: bank card transactions, cash withdrawals, PickMe rides, UberEats orders, a gym membership auto-debited from a secondary account, and two subscription services he intended to cancel six months ago. His rent is LKR 34,000.00 though utilities fluctuate the effective monthly figure. Beyond committed costs, everything else is opaque. He once reviewed three months of bank statements and discovered he had spent LKR 24,000 on coffee and casual dining in a single month. The discovery was genuinely shocking to him. 

He has no category system. He has no concept of what proportion of his income is discretionary versus committed. He has never seen a visual representation of how his spending is distributed. He is certain that such a view would be immediately clarifying and that he has simply never had a tool that produced it. 

##### **<u>3.4 The Goal That Does Not Move</u>** 

Kavindu intends to purchase a MacBook Pro M4. This is not a vanity purchase his current laptop throttles under compilation workloads and is unsuitable for the local development environment his freelance clients increasingly require. He has priced his preferred configuration at approximately LKR 490,000 from an authorised reseller. 

Eight months ago, he created a Google Sheet titled 'MacBook Fund' with columns for monthly income, expenses, and savings. He populated it diligently for thirteen days. A freelance deadline consumed the following week. When he returned to the sheet three weeks later, the incomplete entries felt more discouraging than helpful and he did not open it again. He does not know what 

**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**Year 3 - Semester 2 - 2026** 

**A S S I G N M E NT 01** a 

monthly savings rate is required to reach his target within twelve months, because he does not know what he currently saves. The goal is emotionally present and financially invisible. 

##### **<u>3.5 The Failed Attempts</u>** 

This is not Kavindu's first attempt to solve this problem. Over the past year, he has tried the following: 

1. A Google Sheet — abandoned when manually entered formulas broke after row insertions, and the overhead of data entry exceeded his willingness to maintain it. 

2. A Play Store budgeting application — uninstalled within nine days because it supported only USD, had no cloud synchronisation, and its information architecture assumed a single income source and a fixed monthly budget. 

3. A notes application where he pasted bank transaction exports — functional for approximately four days before becoming an unreadable block of unlabelled figures. 

4. His bank's native spending summary — which captured only card transactions, omitted cash and secondary-account activity entirely, and miscategorised a PickMe fare as 'online retail'. 

Each failure had a proximate cause. The underlying cause was consistent: none of these tools were designed for someone with irregular multi-source income, impulsive discretionary spending, a specific quantified goal, and a low tolerance for friction in daily data entry. They were either too rigid, too manual, too narrow in their data model, or too unpleasant to open every day. 

##### **<u>3.6  What a Solution Needs to Be</u>** 

Kavindu has never written a requirements document. But a careful, patient conversation with him would surface in complaints, in hesitations, in wishful thinking — a coherent picture of what he actually needs. That picture is embedded in the pages above. Finding it, translating it into a technical specification, and building something that would genuinely change Kavindu's relationship with his money is your assignment. 

This scenario is your specification. There is no feature list. There are no pre-written **SPECIAL** user stories. There are no wireframes. Read it until you understand not just what **NOTE** Kavindu needs, but why every previous tool failed him and what a system designed s <mark>p</mark> ecificall <mark>y</mark> for someone like him would have to do differentl <mark>y</mark> . 

# ~~eC~~ **<mark>4.  Your Task — From Scenario</mark>** **<u><mark>to System</mark></u>** 

##### **<u>4.1 Phase One — Analysis</u>** 

Your work begins before the IDE. You are expected to conduct a structured analysis of Kavindu's scenario. This analysis must be documented formally and will carry direct weight in the evaluation. The following questions are not instructions they are analytical lenses. Your answers, whether explicit in your documentation or implicit in your design decisions, will be assessed. 

- Kavindu earns from four sources with different currencies, frequencies, and certainty levels. What must a system understand about income structure in order to give a user an accurate picture of what they actually earn monthly — not what they estimate? 

- Kavindu has failed with four different tools. What do these failures share at a behavioural and UX level? What architectural or design decisions in your application would prevent the same outcome? 

- Kavindu has a specific, quantified savings goal and a deadline. His previous tools never connected daily financial behaviour to that goal in a visible or motivating way. How should 

**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**A S S I G N M E NT 01** ypN 

**Year 3 - Semester 2 - 2026** 

a well-designed system make goal progress feel real and financially grounded — not abstract? 

- Given that your application cannot import bank data automatically, how should it handle expense entry in a way that minimises friction and maximises long-term user adherence? What must an expense record capture to be analytically useful at the end of a month? 

- Kavindu has no concept of his discretionary-to-committed spending ratio. What visualisations or summary views would most efficiently reveal this to a user who does not think in financial categories? What is the minimum set of views that would give him meaningful financial self-awareness within the first week? 

##### **<u>4.2 Phase Two — Design</u>** 

Produce the following design artefacts, all of which must appear in your documentation submission: 

- A structured requirements document distinguishing functional requirements, non-functional requirements, and constraints — derived directly from your reading of the scenario. 

- A Firebase Firestore schema with justification for collection structure, indexing strategy, and multi-currency income handling. 

- An MVVM architecture diagram mapping your screens, ViewModels, Repositories, and data sources. 

- A minimum of three annotated wireframes or high-fidelity mockups covering the screens most critical to Kavindu's daily experience. 

##### **4.3 Phase Three — Implementation** 

Build the application. It will be evaluated not merely on technical correctness but on whether it would genuinely improve Kavindu's financial clarity. The following constraints apply without exception: 

- The application must be written entirely in Kotlin. No Java interop in the application layer. 

- All UI must be implemented with Jetpack Compose. No XML layouts will be accepted. 

- MVVM architecture must be strictly observed. ViewModel logic must not reside in Composable functions. Data layer calls must not originate from the UI layer. 

- Firebase Authentication must manage user identity. Hard-coded user data will receive a mark of zero for the Firebase criterion. 

- Firebase Firestore must serve as the primary persistence layer. Firestore Security Rules must be submitted as part of your deliverables. 

- The application must install and run correctly on Android API Level 26 (Android 8.0) and above, targeting API 34 or higher. 

- All basic error states must be handled, no unhandled exceptions, no blank screens on network failure, no crashes on empty datasets. 

#### **<u><mark>5. Technical Requirements</mark></u>** 

|**Layer / Concern**|**Required Technology**|**Constraints & Notes**|
|---|---|---|
|**Language**|Kotlin (100%)|No Java files in application layer. Kotlin<br>DSL for Gradle.|



**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**A S S I G N M E NT 01** FN 

**Year 3 - Semester 2 - 2026** 

|**Layer / Concern**|**Required Technology**|**Constraints & Notes**|
|---|---|---|
|**UI Framework**|Jetpack Compose|Material Design 3 components. No XML<br>layouts accepted.|
|**Navigation**|Navigation Compose|Single-activity<br>architecture.<br>Typed<br>arguments preferred.|
|**Architecture Pattern**|MVVM|Strict separation: UI → ViewModel →<br>Repository→ Model.|
|**State Management**|StateFlow / LiveData|No raw mutable state exposed from<br>ViewModel to UI layer.|
|**Async & Concurrency**|Kotlin Coroutines|viewModelScope for all async operations.|
|**Authentication**|Firebase Authentication|Email/password mandatory. Google Sign-<br>In is bonus credit.|
|**Database**|Firebase Firestore|Real-time<br>listeners<br>on<br>dashboard.<br>Securityrules required.|
|**Local Caching**|Room (strongly recommended)|Offline persistence. Do not block UI on<br>network unavailability.|
|**Dependency Injection**|Hilt (preferred) or Koin|No manual ViewModel instantiation inside<br>Composable functions.|
|**Minimum SDK**|API Level 26 — Android 8.0 Oreo|Target SDK 34 or above. APK must install<br>on Android 8.0+.|



#### **<u><mark>6. Deliverables</mark></u>** 

##### **<u>6.1 Source Code Repository</u>** 

- Private GitHub or GitLab repository. Submit the access link via the LMS before the deadline. 

- • Commit history must reflect genuine, iterative development. A single-commit repository will be referred for academic integrity review. 

- README.md must include: project overview, Firebase configuration steps, build instructions, and an architectural summary. 

##### **<u>6.2 Technical Design Document</u>** 

- Professionally formatted PDF — minimum 15 pages, no maximum. 

- Must contain: requirements analysis, Firestore schema, MVVM diagram, annotated wireframes, and design decision justifications. 

- Documentation that does not reference Kavindu's specific scenario or your specific implementation will be penalised. 

##### **<u>6.3 APK File</u>** 

- A release-signed or debug APK connecting to a live Firebase project. Local emulator configurations are not acceptable for submission. 

- Validated on a physical device or correctly configured emulator running Android 8.0+. 

##### **<u>6.4 Live Demo & Presentation</u>** 

**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**A S S I G N M E NT 01** ET-. 

**Year 3 - Semester 2 - 2026** 

- A 15-minute scheduled session with the lecture panel during the demonstration week published on the LMS. 

- You will run the application live, walk through the core user flows, and explain your architectural and schema decisions. 

- The session is an engineering discussion, not a slide deck presentation. Prepare to answer direct questions about your ViewModel structure, Firestore queries, and requirement derivation process. 

Code sharing between students, submission of AI-generated code as original work without disclosure, and plagiarism of open-source finance applications will result in **ACADEMIC** an automatic zero and referral to the Faculty Disciplinary Committee. AI tools may be **INTEGRITY** used as a learning aid, but all code, design decisions, and documentation submitted must re <mark>p</mark> resent <mark>y</mark> our own intellectual work. 

#### **<mark>7. Evaluation Criteria</mark>** 

The rubric below governs all assessment. Marks are weighted toward problem understanding, architecture, and demonstration not toward feature volume. A focused, well-reasoned application that solves Kavindu's core problem with architectural integrity will outscore a feature-dense application with structural weaknesses. Read this rubric before you begin your analysis. 

|**#**|**Evaluation Criterion**|**What Is Being Assessed**|**Marks**|**Weight**|
|---|---|---|---|---|
|1|**Problem**<br>**Analysis**<br>**&**<br>**Requirement Extraction**|Quality and depth of the derived functional and non-<br>functional requirements from the scenario. Evidence<br>of independent thinking systems. Absence of generic<br>feature listing.|15|15%|
|2|**Architecture**<br>**&**<br>**Code**<br>**Quality**|Correct and clean MVVM implementation. Separation<br>of UI, ViewModel, Repository, and Model. Use of<br>Kotlin coroutines, StateFlow, dependency injection.<br>No business logic in Composables.|15|15%|
|3|**Jetpack Compose UI &**<br>**UX Design**|Composable decomposition quality. Navigation graph<br>integrity. Adherence to Material Design 3. Extent to<br>which the design genuinely resolves Kavindu's<br>usabilityfailures — not merelyfunctional screens.|15|15%|
|4|**Firebase Integration**|Firebase Authentication implementation. Firestore<br>data modelling — collections, indexing, multi-<br>currency<br>handling.<br>Real-time<br>listeners.<br>Offline<br>persistence. Submitted securityrules.|15|15%|
|5|**Feature Completeness &**<br>**Innovation**|Core problem coverage. Handling of edge cases<br>(irregular<br>income,<br>multi-source<br>entries,<br>goal<br>progress). Meaningful enhancements beyond the<br>minimum thatgenuinelyimprove the solution.|10|10%|
|6|**Technical**<br>**Documentation**|Requirements<br>document,<br>Firestore<br>schema,<br>architecture<br>diagram,<br>annotated<br>wireframes,<br>README. Specificity to Kavindu's scenario —<br>generic or templated documentation will bepenalised.|10|10%|



**BSc (Hons) in Computer Science SE3092: Platform Based Development SE3092: Platform Based Development** 

**Year 3 - Semester 2 - 2026** 

**A S S I G N M E NT 01** ypN 

|**#**<br>**Evaluation Criterion**|**What Is Being Assessed**|**Marks**|**Weight**|
|---|---|---|---|
|7<br>**Demo & Presentation**|Clarity and confidence in presenting the solution.<br>Ability to explain architectural decisions, Firestore<br>schema,<br>and<br>MVVM<br>decomposition<br>under<br>questioning. APK runs live without errors.|20|20%|
|**TOTAL**||**100**|**100%**|



Partial credit is available within every criterion. A weak Firebase implementation scores partial marks rather than zero. However, absent components, no Firebase **PARTIAL** Authentication, no submitted security rules, an APK that fails to install, or no **CREDIT** attendance at the demo session will receive zero for the relevant criterion without exce <mark>p</mark> tion. 

#### **<u><mark>8. Special Note</mark></u>** 

The ability to navigate from that starting point to a shipped, functional product that a real user would actually open every day is not a skill that emerges from following instructions. It is a skill built through practice, through the discipline of thinking before building, and through honest selfassessment of whether what you have built actually solves the problem or merely resembles a solution. 

Kavindu's problem is technically approachable. The architecture is well-documented. The Firebase APIs are mature. The challenge here is not the Kotlin or the Compose. it is understanding the problem with enough precision and empathy to know exactly what to build, and caring enough about the user experience to build it well. 

If your submission makes us believe genuinely believe that Kavindu would open this application daily, that it would change how he understands his money, and that he would still be using it six months from now, you will have done this assignment justice. We look forward to your work. 

### **<u><mark>SE3092 - Platform Based Development | Detailed Assignment Marking Rubric</mark></u>** 

|**Criterion**|**Weight**|**Excellent**<br>**(75–100%)**|**Good**<br>**(60–75%)**|**Average**<br>**(45–60%)**|**Below Average**<br>**(25–45%)**|**Poor**<br>**(0–25%)**|**Expected**<br>**Deliverables**|
|---|---|---|---|---|---|---|---|
|**Problem**<br>**Analysis &**<br>**Requirement**<br>**Extraction**|15%|Derives comprehensive<br>functional & non-functional<br>requirements independently<br>from the scenario; zero generic<br>feature listing; demonstrates<br>deep critical thinking.|Covers most requirements<br>with minor gaps; some<br>independent analysis<br>evident; mostly specific to<br>the scenario.|Basic requirements<br>listed but largely<br>generic; limited depth<br>or analysis; may miss<br>non-functional<br>requirements.|Requirements are<br>superficial or partially<br>generic; little independent<br>analysis; significant gaps.|Requirements are<br>superficial, generic,<br>or copied; little or no<br>independent<br>analysis.|Software<br>Requirements<br>Specification (SRS)<br>Use case diagram|
|**Architecture &**<br>**Code Quality**|15%|Clean MVVM with clear<br>UI/ViewModel/Repository/Model<br>separation; Kotlin coroutines &<br>StateFlow used correctly; zero<br>business logic in Composables.|MVVM mostly correct with<br>minor violations; coroutines<br>& StateFlow present; small<br>logic leaks into<br>Composables.|Partial MVVM;<br>coroutines or StateFlow<br>used inconsistently;<br>some business logic in<br>Composables.|MVVM attempted but<br>largely incorrect; minimal<br>coroutines/StateFlow;<br>frequent logic in UI layer.|No clear<br>architecture;<br>minimal or incorrect<br>separation;<br>business logic<br>mixed into UI.|MVVM folder<br>structure<br>ViewModel &<br>Repository classes<br>StateFlow /<br>coroutine usage|
|**Jetpack**<br>**Compose**<br>**UI & UX Design**|15%|Composables decomposed<br>optimally; full Navigation graph;<br>Material Design 3 adhered to;<br>design genuinely resolves<br>Kavindu's usability failures.|Good Composable<br>structure; navigation mostly<br>correct; Material 3 mostly<br>followed; addresses most<br>usability issues.|Basic Composables;<br>navigation partially<br>implemented; partial<br>Material 3; minimal UX<br>improvement.|Poor decomposition;<br>navigation incomplete;<br>Material 3 largely<br>ignored; usability issues<br>mostly unaddressed.|Poor<br>decomposition;<br>navigation broken<br>or absent; ignores<br>Material 3; usability<br>issues not<br>addressed.|Navigation graph<br>Composable screen<br>files<br>Material Design 3<br>theme<br>Annotated UI<br>wireframes|
|**Firebase**<br>**Integration**|15%|Auth fully implemented;<br>Firestore collections/indexing &<br>multi-currency modelled<br>correctly; real-time listeners &<br>offline persistence; security<br>rules submitted.|Auth and Firestore working;<br>most data modelling<br>correct; listeners present;<br>minor gaps in rules or<br>offline.|Basic Auth and<br>Firestore; limited<br>indexing or multi-<br>currency handling; no<br>listeners or offline;<br>partial rules.|Auth or Firestore partially<br>working; data model<br>incorrect; no listeners; no<br>security rules.|Firebase barely<br>functional; incorrect<br>data modelling; no<br>security rules; no<br>listeners or<br>persistence.|Firebase<br>Authentication setup<br>Firestore schema<br>diagram<br>Security rules file<br>(firestore.rules)<br>Real-time listener<br>implementation|
|**Feature**<br>**Completeness**<br>**& Innovation**|10%|All core problem areas covered<br>including edge cases (irregular<br>income, multi-source entries,<br>goal progress); meaningful<br>enhancements that genuinely<br>improve the solution.|Core features present;<br>handles most edge cases;<br>at least one meaningful<br>enhancement.|Core features mostly<br>present; edge cases<br>largely ignored;<br>enhancements are<br>superficial or missing.|Several core features<br>missing; no edge case<br>handling; no meaningful<br>innovation.|Key features<br>missing; edge<br>cases not handled;<br>no innovation<br>beyond minimum<br>requirements.|Core feature<br>implementation<br>Edge case handling<br>Additional features<br>beyond minimum|



|**Technical**<br>**Documentation**|10%|Requirements doc, Firestore<br>schema, architecture diagram,<br>annotated wireframes &<br>README all present; entirely<br>specific to Kavindu's scenario;<br>professional quality.|Most documentation<br>present; mostly scenario-<br>specific; minor gaps or<br>quality issues.|Some documentation<br>present; partially<br>generic; missing key<br>artefacts (e.g., schema<br>or diagram).|Documentation sparse;<br>largely generic or<br>templated; missing<br>multiple key artefacts.|Documentation<br>largely absent,<br>generic/templated,<br>or not specific to the<br>given scenario.|SRS document<br>Architecture diagram<br>Firestore schema<br>diagram<br>Annotated<br>wireframes<br>README|
|---|---|---|---|---|---|---|---|
|**Demo &**<br>**Presentation**|20%|Clear and confident explanation<br>of architectural decisions,<br>Firestore schema & MVVM<br>decomposition; handles<br>questions well; APK runs live<br>without errors.|Good explanation of most<br>design decisions; handles<br>most questions; APK runs<br>with minor issues.|Basic walk-through;<br>struggles with deeper<br>questions; APK may<br>have errors or require<br>reloading.|Weak explanation;<br>unable to answer most<br>questions; APK unstable<br>or partially demonstrated.|Unable to explain<br>design decisions;<br>cannot answer<br>questions; APK fails<br>or is not<br>demonstrated.|Slide deck<br>Live APK<br>demonstration<br>Q&A on design and<br>architecture<br>decisions|



