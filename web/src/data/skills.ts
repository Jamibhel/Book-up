export interface Skill {
  id: string;
  name: string;
  category: string;
  description: string;
  iconName: string;
  gradientFrom: string;
  gradientTo: string;
  accentColor: string;
}

export const REDESIGN_SKILLS: Skill[] = [
  // Technology & Coding
  {
    id: "web-dev",
    name: "Web Development",
    category: "Technology",
    description: "Build modern, responsive web applications using React, Node.js, and next-gen frameworks.",
    iconName: "Code",
    gradientFrom: "from-emerald-500",
    gradientTo: "to-teal-600",
    accentColor: "#10B981"
  },
  {
    id: "python",
    name: "Python & Scripting",
    category: "Technology",
    description: "Master Python programming for automation, scripting, and backend system development.",
    iconName: "Terminal",
    gradientFrom: "from-emerald-600",
    gradientTo: "to-green-500",
    accentColor: "#059669"
  },
  {
    id: "mobile-dev",
    name: "Mobile App Dev",
    category: "Technology",
    description: "Create premium native and cross-platform mobile apps for iOS and Android.",
    iconName: "Smartphone",
    gradientFrom: "from-teal-500",
    gradientTo: "to-emerald-700",
    accentColor: "#14b8a6"
  },
  {
    id: "cybersecurity",
    name: "Cybersecurity",
    category: "Technology",
    description: "Understand network defense, secure coding practices, and ethical hacking fundamentals.",
    iconName: "Shield",
    gradientFrom: "from-green-600",
    gradientTo: "to-emerald-800",
    accentColor: "#16a34a"
  },
  {
    id: "cloud-computing",
    name: "Cloud & DevOps",
    category: "Technology",
    description: "Deploy scalable systems using AWS, Docker, Firebase, and continuous integration pipelines.",
    iconName: "Cloud",
    gradientFrom: "from-emerald-400",
    gradientTo: "to-teal-500",
    accentColor: "#34D399"
  },
  {
    id: "ai-prompting",
    name: "AI & Data Science",
    category: "Technology",
    description: "Harness LLMs, build neural networks, and analyze complex datasets with machine learning.",
    iconName: "Cpu",
    gradientFrom: "from-teal-400",
    gradientTo: "to-emerald-600",
    accentColor: "#2dd4bf"
  },

  // Design & Creative
  {
    id: "ui-ux",
    name: "UI/UX Design",
    category: "Design",
    description: "Craft visually stunning, user-centered product experiences, wireframes, and design systems.",
    iconName: "Palette",
    gradientFrom: "from-amber-400",
    gradientTo: "to-orange-500",
    accentColor: "#fbbf24"
  },
  {
    id: "graphic-design",
    name: "Graphic Design",
    category: "Design",
    description: "Develop eye-catching visual brand assets, posters, illustrations, and typography layouts.",
    iconName: "PenTool",
    gradientFrom: "from-yellow-500",
    gradientTo: "to-amber-600",
    accentColor: "#eab308"
  },
  {
    id: "video-vfx",
    name: "Video & Animation",
    category: "Design",
    description: "Shoot, cut, and edit premium cinematic videos and apply advanced special effects.",
    iconName: "Video",
    gradientFrom: "from-amber-500",
    gradientTo: "to-orange-600",
    accentColor: "#f59e0b"
  },

  // Business & Strategy
  {
    id: "entrepreneurship",
    name: "Entrepreneurship",
    category: "Business",
    description: "Build business models, define product-market fit, and craft compelling investment pitches.",
    iconName: "TrendingUp",
    gradientFrom: "from-yellow-600",
    gradientTo: "to-amber-500",
    accentColor: "#d97706"
  },
  {
    id: "digital-marketing",
    name: "Growth Marketing",
    category: "Business",
    description: "Master search engine optimization, content strategy, and viral social campaigns.",
    iconName: "Megaphone",
    gradientFrom: "from-amber-400",
    gradientTo: "to-yellow-500",
    accentColor: "#f59e0b"
  },
  {
    id: "financial-literacy",
    name: "Finance & Investing",
    category: "Business",
    description: "Understand personal budgeting, investment strategy, accounting, and crypto fundamentals.",
    iconName: "Coins",
    gradientFrom: "from-emerald-500",
    gradientTo: "to-amber-400",
    accentColor: "#10B981"
  },

  // Languages & Communication
  {
    id: "french",
    name: "French Language",
    category: "Languages",
    description: "Develop conversational proficiency, perfect your accent, and learn grammar.",
    iconName: "Globe",
    gradientFrom: "from-teal-600",
    gradientTo: "to-green-600",
    accentColor: "#0d9488"
  },
  {
    id: "public-speaking",
    name: "Public Speaking",
    category: "Languages",
    description: "Deliver impactful speeches, overcome stage fright, and command any room.",
    iconName: "Mic",
    gradientFrom: "from-amber-500",
    gradientTo: "to-yellow-600",
    accentColor: "#f59e0b"
  },

  // Advanced Academics
  {
    id: "calculus",
    name: "Advanced Calculus",
    category: "Academics",
    description: "Conquer limits, derivatives, integrals, differential equations, and proofs.",
    iconName: "BookOpen",
    gradientFrom: "from-emerald-600",
    gradientTo: "to-teal-800",
    accentColor: "#059669"
  },
  {
    id: "statistics",
    name: "Data & Probability",
    category: "Academics",
    description: "Apply hypothesis testing, regression analysis, and statistical distributions.",
    iconName: "BarChart2",
    gradientFrom: "from-teal-600",
    gradientTo: "to-emerald-900",
    accentColor: "#0d9488"
  },
  {
    id: "physics",
    name: "Quantum Physics",
    category: "Academics",
    description: "Explore electromagnetism, thermodynamics, particle systems, and quantum theory.",
    iconName: "Compass",
    gradientFrom: "from-green-500",
    gradientTo: "to-teal-700",
    accentColor: "#22c55e"
  },

  // Music & Arts
  {
    id: "music-prod",
    name: "Music Production",
    category: "Music",
    description: "Record tracks, engineer beats, understand synthesis, and mix in modern DAWs.",
    iconName: "Music",
    gradientFrom: "from-yellow-400",
    gradientTo: "to-orange-400",
    accentColor: "#facc15"
  },

  // Career Prep
  {
    id: "interview-coaching",
    name: "Interview Prep",
    category: "Career",
    description: "Build a stellar CV, practice mock behavioral interviews, and negotiate offers.",
    iconName: "Award",
    gradientFrom: "from-amber-500",
    gradientTo: "to-yellow-500",
    accentColor: "#f59e0b"
  },
  {
    id: "sat-prep",
    name: "SAT/ACT Prep",
    category: "Career",
    description: "Acing standardized college entrance exams through targeted strategy and practice.",
    iconName: "Layers",
    gradientFrom: "from-emerald-500",
    gradientTo: "to-green-600",
    accentColor: "#10B981"
  }
];

export const CATEGORIZED_SKILLS = {
  "Technology": [
    "Web Development", "Python & Scripting", "Mobile App Dev", "Cybersecurity", "Cloud & DevOps", "AI & Data Science"
  ],
  "Design": [
    "UI/UX Design", "Graphic Design", "Video & Animation"
  ],
  "Business": [
    "Entrepreneurship", "Growth Marketing", "Finance & Investing"
  ],
  "Languages": [
    "French Language", "Public Speaking"
  ],
  "Academics": [
    "Advanced Calculus", "Data & Probability", "Quantum Physics"
  ],
  "Music": [
    "Music Production"
  ],
  "Career": [
    "Interview Prep", "SAT/ACT Prep"
  ]
};
