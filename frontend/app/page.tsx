"use client";
import { useEffect, useState } from 'react';
type Decision = { id: string; title: string; description: string; status: 'ELIGIBLE'|'UNKNOWN'|'INELIGIBLE'; checks: {attribute:string;status:string;explanation:string}[] };
type Attributes = Record<string,string|number|boolean|null>;
async function api<T>(path:string, init?:RequestInit):Promise<T> {
 const res=await fetch(`/api/${path}`,{...init,headers:{'Content-Type':'application/json',...init?.headers}});
 if(!res.ok) { const body=await res.json().catch(()=>null); throw new Error(body?.message || `Request failed (${res.status})`); }
 return res.json();
}
export default function Home() {
 const [attributes,setAttributes]=useState<Attributes>({});
 const [programs,setPrograms]=useState<Decision[]>([]);
 const [gaps,setGaps]=useState<string[]>([]);
 const [enrolled,setEnrolled]=useState<string[]>([]);
 const [busy,setBusy]=useState(true);
 const [message,setMessage]=useState('');
 const [error,setError]=useState('');
 const [search,setSearch]=useState('');
 async function refresh() {
  const [profile, decisions, missing, enrollments]=await Promise.all([
   api<{attributes:Attributes}>('profile'),api<Decision[]>('programs'),api<string[]>('gaps'),api<{program_id:string}[]>('enrollments')
  ]);
  setAttributes(profile.attributes);setPrograms(decisions);setGaps(missing);setEnrolled(enrollments.map(e=>e.program_id));
 }
 useEffect(()=>{refresh().catch(e=>setError(e.message)).finally(()=>setBusy(false));},[]);
 async function save(event:React.FormEvent) {
  event.preventDefault();setBusy(true);setError('');setMessage('');
  try { await api('profile',{method:'PUT',body:JSON.stringify({attributes})});await refresh();setMessage('Profile saved. Program decisions have been refreshed.'); }
  catch(e) {setError(e instanceof Error?e.message:'Unable to save profile');} finally {setBusy(false);}
 }
 async function enroll(id:string) {
  setBusy(true);setError('');setMessage('');
  try {await api(`programs/${id}/enroll`,{method:'POST'});await refresh();setMessage('Enrollment recorded.');}
  catch(e) {setError(e instanceof Error?e.message:'Unable to enroll');} finally {setBusy(false);}
 }
 function update(key:string,value:Attributes[string]){setAttributes(prev=>({...prev,[key]:value}));}
 return <><header><span className="brand">BenefitBridge</span><span>Independent demo · synthetic programs</span></header>
 <main id="main"><section className="intro"><p className="eyebrow">MORE CLARITY. FEWER GUESSWORK.</p><h1>Find your next opportunity.</h1><p>Explore sample programs, see what qualifies, and understand what information is still needed.</p></section>
 <p role="alert" className="error">{error}</p><p role="status">{message || (busy?'Updating…':'')}</p>
 <div className="workspace"><aside><form onSubmit={save}><h2>Your demo profile</h2><p>Self-reported values are for this local demo only.</p>
 <label htmlFor="age">Age</label><input id="age" type="number" min="0" max="130" value={attributes.age==null?'':String(attributes.age)} onChange={e=>update('age',e.target.value===''?null:Number(e.target.value))}/>
 <label htmlFor="residency">State of residency</label><input id="residency" maxLength={2} placeholder="NY" value={String(attributes.residency??'')} onChange={e=>update('residency',e.target.value.toUpperCase()||null)}/>
 {(['student','disability'] as const).map(key=><div key={key}><label htmlFor={key}>{key==='student'?'Current student':'Qualifying disability'}</label><select id={key} value={attributes[key]==null?'unknown':String(attributes[key])} onChange={e=>update(key,e.target.value==='unknown'?null:e.target.value==='true')}><option value="unknown">Not provided</option><option value="true">Yes</option><option value="false">No</option></select></div>)}
 <button disabled={busy} type="submit">Save and refresh</button></form>
 <section className="gaps"><h3>Missing information</h3>{gaps.length?<ul>{gaps.map(g=><li key={g}>{g}</li>)}</ul>:<p>No missing program attributes.</p>}</section></aside>
 <section aria-labelledby="program-title" aria-busy={busy}><h2 id="program-title">Explore programs</h2><label htmlFor="search">Filter by name</label><input id="search" type="search" value={search} onChange={e=>setSearch(e.target.value)}/>
 <p>Eligible programs appear first. Missing information and unmet conditions remain visible.</p>
 {programs.filter(p=>p.title.toLowerCase().includes(search.toLowerCase())).map(p=><article key={p.id}><span className={`badge ${p.status.toLowerCase()}`}>{p.status==='UNKNOWN'?'Information needed':p.status==='ELIGIBLE'?'Eligible':'Not currently eligible'}</span><h3>{p.title}</h3><p>{p.description}</p><details><summary>Why this result?</summary><ul>{p.checks.map((c,i)=><li key={i}><strong>{c.status==='ELIGIBLE'?'Met':c.status==='UNKNOWN'?'Missing':'Unmet'}:</strong> {c.explanation}</li>)}</ul></details><button disabled={busy||p.status!=='ELIGIBLE'||enrolled.includes(p.id)} onClick={()=>enroll(p.id)}>{enrolled.includes(p.id)?'Enrolled':'Enroll in demo'}</button></article>)}
 {!busy&&!programs.filter(p=>p.title.toLowerCase().includes(search.toLowerCase())).length&&<p>No matching programs.</p>}
 </section></div><footer>These fictional programs do not determine eligibility for real benefits. Enrollment uses a shared local demo member.</footer></main></>;
}
