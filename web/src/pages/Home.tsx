import { Star, ArrowRight, Smartphone, Zap, Shield, Users, Globe, Award } from 'lucide-react';
import { Link } from 'react-router-dom';
import * as Icons from 'lucide-react';
import { REDESIGN_SKILLS } from '../data/skills';

const NIGERIAN_NAMES = [
  'Chinedu', 'Amina', 'Oluwaseun', 'Ngozi', 'Ibrahim'
];

export default function Home() {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-transparent">
      
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 md:pt-20 pb-20 px-4 md:px-8 max-w-7xl mx-auto">
        <div className="flex flex-col lg:flex-row items-center justify-between gap-16 relative z-10">
          <div className="flex-1 space-y-8 text-center lg:text-left">
            <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-bookup-primary/10 dark:bg-bookup-accent/10 text-bookup-primary dark:text-bookup-accent font-bold text-sm border border-bookup-primary/20 backdrop-blur-md">
              <Star size={16} className="fill-bookup-accent text-bookup-accent" />
              <span>Nigeria's #1 Learning Community</span>
            </div>

            <h1 className="text-6xl md:text-7xl lg:text-8xl font-black text-gray-900 dark:text-white leading-[0.95] tracking-tight">
              Master your <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-bookup-primary to-bookup-accent">
                future
              </span> today.
            </h1>

            <p className="text-xl text-gray-600 dark:text-gray-400 font-medium max-w-xl mx-auto lg:mx-0 leading-relaxed">
              Connect with Nigeria's top 1% of educators. Personalized tutoring, premium resources, and a community that cares about your success.
            </p>

            <div className="flex flex-col sm:flex-row items-center gap-4 justify-center lg:justify-start pt-4">
              <Link to="/signup" className="w-full sm:w-auto bg-gradient-to-r from-bookup-primary to-bookup-primary-dark text-white px-10 py-5 rounded-full font-black text-lg hover:scale-105 transition-all shadow-xl shadow-bookup-primary/20 flex items-center justify-center gap-3 border-b-2 border-bookup-accent">
                Join Now <ArrowRight size={22} />
              </Link>
              <a href="#download" className="w-full sm:w-auto bg-white dark:bg-gray-950/40 text-gray-900 dark:text-white border border-gray-200/50 dark:border-white/5 px-10 py-5 rounded-full font-black text-lg hover:bg-gray-50 dark:hover:bg-white/5 transition-all flex items-center justify-center gap-3 shadow-md">
                 <Smartphone size={22} className="text-bookup-accent" />
                 Download App
              </a>
            </div>

            <div className="flex flex-col sm:flex-row items-center gap-6 justify-center lg:justify-start pt-8 border-t border-gray-200/50 dark:border-white/5 mt-8">
              <div className="flex -space-x-4">
                {NIGERIAN_NAMES.map((name, i) => (
                  <img 
                    key={i} 
                    src={`https://ui-avatars.com/api/?name=${name}&background=random&color=fff&bold=true`} 
                    className="w-14 h-14 rounded-full border-4 border-white dark:border-gray-900 shadow-lg object-cover" 
                    alt={`Student ${name}`} 
                  />
                ))}
              </div>
              <div className="text-center sm:text-left">
                <div className="flex items-center justify-center sm:justify-start gap-1">
                  {[1,2,3,4,5].map(i => <Star key={i} size={18} className="fill-bookup-accent text-bookup-accent" />)}
                </div>
                <p className="font-black text-gray-800 dark:text-white mt-1">10k+ active students</p>
              </div>
            </div>
          </div>

          <div className="flex-1 relative w-full max-w-lg lg:max-w-none hidden lg:block animate-float">
            <div className="relative p-12">
               <div className="absolute top-0 right-0 w-72 h-72 bg-bookup-primary/10 rounded-full blur-[100px] -z-10"></div>
               <div className="absolute bottom-0 left-0 w-72 h-72 bg-bookup-accent/10 rounded-full blur-[100px] -z-10"></div>
               
               <div className="bg-white dark:bg-gray-950/40 rounded-[3rem] p-10 shadow-2xl border border-gray-200/50 dark:border-white/5 relative glass-card">
                  <div className="flex items-center justify-between mb-10">
                     <div className="space-y-1">
                        <p className="text-xs font-black text-bookup-accent uppercase tracking-widest">Active Now</p>
                        <h3 className="text-2xl font-black text-gray-900 dark:text-white">Virtual Classroom</h3>
                     </div>
                     <div className="w-14 h-14 bg-bookup-primary/10 text-bookup-accent rounded-2xl flex items-center justify-center">
                        <Zap size={26} />
                     </div>
                  </div>
                  <div className="space-y-6">
                     {[
                       { name: 'Dr. Adewale', subject: 'Calculus III' },
                       { name: 'Prof. Okon', subject: 'Organic Chem' },
                     ].map((tutor, i) => (
                       <div key={i} className="flex items-center gap-5 p-5 bg-gray-50 dark:bg-gray-900/10 rounded-2.5rem border border-gray-200/50 dark:border-white/5 hover:bg-white dark:hover:bg-gray-950/60 hover:shadow-xl transition-all group">
                          <img src={`https://ui-avatars.com/api/?name=${tutor.name}&background=2E8B57&color=fff&bold=true`} className="w-16 h-16 rounded-xl object-cover" />
                          <div className="flex-1">
                             <p className="font-black text-gray-900 dark:text-white text-lg">{tutor.name}</p>
                             <p className="text-gray-500 dark:text-gray-400 font-bold">{tutor.subject}</p>
                          </div>
                          <div className="w-10 h-10 bg-white dark:bg-gray-900 rounded-xl flex items-center justify-center text-bookup-primary dark:text-bookup-accent shadow-sm opacity-0 group-hover:opacity-100 transition-opacity">
                             <ArrowRight size={20} />
                          </div>
                       </div>
                     ))}
                  </div>
                  <button className="w-full mt-10 bg-gray-950 dark:bg-white text-white dark:text-gray-950 font-black py-5 rounded-2xl text-lg hover:bg-bookup-accent hover:text-gray-950 transition-all shadow-xl">
                    Join Session
                  </button>
               </div>
            </div>
          </div>
        </div>
      </section>

      {/* Redesigned Skills Explorer Section */}
      <section className="py-24 px-4 bg-white dark:bg-gray-950/40 border-t border-b border-gray-200/50 dark:border-white/5">
        <div className="max-w-7xl mx-auto space-y-16">
          <div className="text-center space-y-6 max-w-3xl mx-auto">
            <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600 dark:text-emerald-400 font-bold text-xs uppercase tracking-widest border border-emerald-200/20">
              <Icons.Compass size={14} className="animate-spin-slow" /> Explore Disciplines
            </div>
            <h2 className="text-4xl md:text-6xl font-black text-gray-900 dark:text-white font-display tracking-tight leading-none">
              Learn Anything. <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-bookup-primary via-bookup-accent to-emerald-500">20 Premium Skills</span> at your fingertips.
            </h2>
            <p className="text-lg text-gray-500 dark:text-gray-400 font-medium leading-relaxed">
              Unlock expert-led mentoring across high-demand technical domains, creative arts, and academic excellence.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {REDESIGN_SKILLS.map((skill) => {
              const IconComponent = (Icons as any)[skill.iconName] || Icons.BookOpen;
              return (
                <div 
                  key={skill.id} 
                  className="group relative bg-gray-50 dark:bg-gray-900/10 p-8 rounded-[2rem] border border-gray-200/40 dark:border-white/5 hover:border-transparent hover:shadow-2xl transition-all duration-500 hover:-translate-y-1 overflow-hidden"
                >
                  {/* Glowing background on hover */}
                  <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${skill.gradientFrom} ${skill.gradientTo} opacity-[0.02] group-hover:opacity-[0.08] rounded-full -mr-12 -mt-12 group-hover:scale-150 transition-all duration-700`}></div>
                  
                  <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${skill.gradientFrom} ${skill.gradientTo} text-white flex items-center justify-center mb-6 shadow-lg group-hover:rotate-3 transition-all`}>
                    <IconComponent size={22} />
                  </div>
                  
                  <span className="text-[10px] font-black uppercase tracking-[0.25em] text-gray-400 dark:text-gray-500 mb-2 block">{skill.category}</span>
                  <h4 className="text-xl font-black text-gray-900 dark:text-white mb-3 group-hover:text-bookup-primary dark:group-hover:text-bookup-accent transition-colors">{skill.name}</h4>
                  <p className="text-gray-500 dark:text-gray-400 text-sm font-medium leading-relaxed line-clamp-3">{skill.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="py-24 bg-gray-50 dark:bg-gray-950/20 px-4">
         <div className="max-w-7xl mx-auto space-y-20">
            <div className="text-center space-y-6 max-w-3xl mx-auto">
               <h2 className="text-4xl md:text-5xl font-black text-gray-900 dark:text-white font-display">Why students love BookUp</h2>
               <p className="text-xl text-gray-500 dark:text-gray-400 font-medium leading-relaxed">Everything you need to excel in your studies, built for the modern Nigerian student.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
               {[
                 { title: 'Verified Tutors', desc: 'Every tutor is vetted for quality and experience.', icon: Shield, color: 'bg-emerald-600' },
                 { title: 'Live Classes', desc: 'Real-time video sessions with interactive whiteboards.', icon: Zap, color: 'bg-bookup-accent text-gray-950' },
                 { title: 'Community', desc: 'Learn together with peers from all over Nigeria.', icon: Users, color: 'bg-bookup-primary' },
                 { title: 'Materials', desc: 'Access a massive library of notes and past questions.', icon: Globe, color: 'bg-indigo-600' },
               ].map((feature, i) => (
                 <div key={i} className="bg-white dark:bg-gray-950/40 p-10 rounded-[2rem] border border-gray-200/50 dark:border-white/5 shadow-sm hover:shadow-xl hover:border-bookup-accent/20 transition-all group">
                    <div className={`w-14 h-14 ${feature.title === 'Live Classes' ? 'bg-bookup-accent text-gray-950' : 'bg-bookup-primary text-white'} rounded-2xl flex items-center justify-center mb-8 shadow-lg group-hover:scale-110 transition-transform`}>
                       <feature.icon size={26} />
                    </div>
                    <h4 className="text-2xl font-black text-gray-900 dark:text-white mb-4">{feature.title}</h4>
                    <p className="text-gray-500 dark:text-gray-400 font-bold leading-relaxed">{feature.desc}</p>
                 </div>
               ))}
            </div>
         </div>
      </section>

      {/* Trust Stats */}
      <section className="py-20 bg-gradient-to-br from-gray-950 via-bookup-primary-dark to-black text-white relative shimmer-gold border-t border-b border-white/5">
         <div className="max-w-7xl mx-auto px-4 grid grid-cols-2 md:grid-cols-4 gap-12 text-center relative z-10">
            {[
              { val: '10k+', label: 'Active Students' },
              { val: '500+', label: 'Expert Tutors' },
              { val: '50k+', label: 'Study Files' },
              { val: '98%', label: 'Satisfaction' },
            ].map((stat, i) => (
              <div key={i} className="space-y-2">
                 <p className="text-4xl md:text-5xl font-black text-bookup-accent">{stat.val}</p>
                 <p className="text-xs font-black uppercase tracking-[0.2em] text-white/50">{stat.label}</p>
              </div>
            ))}
         </div>
      </section>

      {/* Bulky Download Section */}
      <section id="download" className="py-24 px-4 bg-gray-50 dark:bg-transparent">
         <div className="max-w-6xl mx-auto">
            <div className="bg-gradient-to-br from-gray-950 via-bookup-primary-dark to-black rounded-[3rem] p-12 md:p-24 text-center text-white relative overflow-hidden shadow-2xl border border-white/5">
               <div className="absolute top-0 right-0 w-96 h-96 bg-bookup-accent/5 rounded-full blur-[120px] -mr-48 -mt-48"></div>
               <div className="relative z-10 space-y-10">
                  <div className="w-20 h-20 bg-white/5 rounded-2xl flex items-center justify-center mx-auto backdrop-blur-md border border-white/10">
                     <Smartphone size={36} className="text-bookup-accent" />
                  </div>
                  <h2 className="text-4xl md:text-6xl font-black leading-tight">Better learning <br /> in your pocket.</h2>
                  <p className="text-lg text-gray-300 font-medium max-w-2xl mx-auto">
                     Get the official BookUp Android app for the fastest experience, real-time alerts, and offline access to all your study materials.
                  </p>
                  <div className="flex flex-col sm:flex-row justify-center gap-6">
                     <Link to="/signup" className="bg-bookup-accent text-gray-950 px-12 py-5 rounded-2xl font-black text-xl hover:scale-105 transition-all flex items-center justify-center gap-3">
                        Sign Up to Download <ArrowRight size={24} />
                     </Link>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-8 pt-12 border-t border-white/5">
                     {[
                       { title: 'Live Alerts', icon: Zap },
                       { title: 'Offline Access', icon: Globe },
                       { title: 'Safe & Secure', icon: Shield },
                     ].map((feat, i) => (
                       <div key={i} className="flex items-center justify-center gap-3 text-white/80 font-black uppercase tracking-widest text-xs">
                          <feat.icon size={20} className="text-bookup-accent" />
                          {feat.title}
                       </div>
                     ))}
                  </div>
               </div>
            </div>
         </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 px-4 relative overflow-hidden">
         <div className="max-w-5xl mx-auto bg-gradient-to-br from-bookup-primary to-bookup-primary-dark rounded-[3rem] p-12 md:p-24 text-center text-white relative shadow-2xl border-b-4 border-bookup-accent">
            <div className="absolute top-0 right-0 p-20 opacity-5">
               <Award size={200} />
            </div>
            <h2 className="text-4xl md:text-6xl font-black mb-8 relative z-10 leading-tight">Ready to boost your <br /> academic performance?</h2>
            <p className="text-xl text-white/80 font-medium mb-12 max-w-2xl mx-auto relative z-10">
               Join thousands of students who are already succeeding with BookUp. Your journey starts here.
            </p>
            <div className="flex flex-col sm:flex-row gap-6 justify-center relative z-10">
               <Link to="/signup" className="bg-bookup-accent text-gray-950 px-12 py-5 rounded-2xl font-black text-xl hover:scale-105 transition-all shadow-xl">
                  Get Started for Free
               </Link>
               <Link to="/login" className="bg-white/10 text-white border border-white/20 px-12 py-5 rounded-2xl font-black text-xl hover:bg-white/20 transition-all">
                  Member Login
               </Link>
            </div>
         </div>
      </section>

      {/* Footer */}
      <footer className="py-20 border-t border-gray-200/50 dark:border-white/5 px-4 bg-white dark:bg-gray-950/20">
         <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-10">
            <div className="flex items-center gap-3">
               <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-bookup-primary to-bookup-accent flex items-center justify-center text-white font-black text-xl shadow-md">B</div>
               <span className="text-2xl font-black text-gray-900 dark:text-white font-display">BookUp</span>
            </div>
            <div className="flex gap-8 text-gray-400 font-bold uppercase tracking-widest text-[10px]">
               <a href="#" className="hover:text-bookup-accent transition-colors">About Us</a>
               <a href="#" className="hover:text-bookup-accent transition-colors">Privacy</a>
               <a href="#" className="hover:text-bookup-accent transition-colors">Terms</a>
               <a href="#" className="hover:text-bookup-accent transition-colors">Support</a>
            </div>
            <p className="text-gray-400 text-sm font-bold">© 2026 BookUp Nigeria. All rights reserved.</p>
         </div>
      </footer>

    </div>
  );
}
