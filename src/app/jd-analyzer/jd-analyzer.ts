import { Component, ViewChild, ElementRef } from '@angular/core';
import { HttpClient, HttpEventType, HttpClientModule } from '@angular/common/http';
import Swal from 'sweetalert2';
import { environment } from '../../environments/environment';
import { NgIf, CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface SkillDictionaryEntry {
  canonical: string;
  patterns: RegExp[];
}

interface RequiredSkill {
  skill: string;
  mandatory: boolean;
}

interface JdAnalysis {
  requiredSkills: RequiredSkill[];
  preferredSkills: string[];
  minYears: number | null;
  maxYears: number | null;
  titleTokens: string[];
  domainTerms: string[];
  educationTerms: string[];
  noSponsorship: boolean;
}

interface ScoreBreakdown {
  requiredSkills: number;
  experience: number;
  titleRelevance: number;
  preferredSkills: number;
  domain: number;
  education: number;
  location: number;
}

interface CandidateResult {
  id: any;
  name: string;
  title: string;
  score: number;
  recommendation: string;
  breakdown: ScoreBreakdown;
  matchedRequiredSkills: string[];
  missingRequiredSkills: string[];
  missingMandatorySkills: string[];
  matchedPreferredSkills: string[];
  experienceDetail: { requiredYears: number | null; candidateYears: number };
  strengths: string[];
  concerns: string[];
  _extractedText: string;
}

const RAW_SKILLS: { canonical: string; synonyms: string[] }[] = [
  { canonical: 'javascript', synonyms: ['javascript', 'js', 'ecmascript'] },
  { canonical: 'typescript', synonyms: ['typescript', 'ts'] },
  { canonical: 'python', synonyms: ['python', 'py'] },
  { canonical: 'java', synonyms: ['java'] },
  { canonical: 'c#', synonyms: ['c#', 'c-sharp', 'csharp'] },
  { canonical: 'c++', synonyms: ['c++', 'cpp'] },
  { canonical: 'go', synonyms: ['golang', 'go'] },
  { canonical: 'ruby', synonyms: ['ruby'] },
  { canonical: 'php', synonyms: ['php'] },
  { canonical: 'kotlin', synonyms: ['kotlin'] },
  { canonical: 'swift', synonyms: ['swift'] },
  { canonical: 'scala', synonyms: ['scala'] },
  { canonical: 'rust', synonyms: ['rust'] },
  { canonical: 'react', synonyms: ['react', 'reactjs', 'react.js', 'react native'] },
  { canonical: 'angular', synonyms: ['angular', 'angularjs', 'angular.js'] },
  { canonical: 'vue', synonyms: ['vue', 'vuejs', 'vue.js'] },
  { canonical: 'next.js', synonyms: ['next.js', 'nextjs'] },
  { canonical: 'html', synonyms: ['html', 'html5'] },
  { canonical: 'css', synonyms: ['css', 'css3', 'sass', 'scss', 'less'] },
  { canonical: 'redux', synonyms: ['redux'] },
  { canonical: 'node', synonyms: ['node', 'nodejs', 'node.js'] },
  { canonical: 'express', synonyms: ['express', 'expressjs', 'express.js'] },
  { canonical: 'spring', synonyms: ['spring', 'spring boot', 'springboot'] },
  { canonical: 'django', synonyms: ['django'] },
  { canonical: 'flask', synonyms: ['flask'] },
  { canonical: 'rails', synonyms: ['rails', 'ruby on rails'] },
  { canonical: '.net', synonyms: ['.net', 'dotnet', 'asp.net'] },
  { canonical: 'laravel', synonyms: ['laravel'] },
  { canonical: 'sql', synonyms: ['sql'] },
  { canonical: 'mysql', synonyms: ['mysql'] },
  { canonical: 'postgresql', synonyms: ['postgresql', 'postgres', 'psql'] },
  { canonical: 'mongodb', synonyms: ['mongodb', 'mongo'] },
  { canonical: 'oracle', synonyms: ['oracle db', 'oracle database', 'oracle'] },
  { canonical: 'redis', synonyms: ['redis'] },
  { canonical: 'dynamodb', synonyms: ['dynamodb'] },
  { canonical: 'cassandra', synonyms: ['cassandra'] },
  { canonical: 'aws', synonyms: ['aws', 'amazon web services'] },
  { canonical: 'azure', synonyms: ['azure', 'microsoft azure'] },
  { canonical: 'gcp', synonyms: ['gcp', 'google cloud', 'google cloud platform'] },
  { canonical: 'docker', synonyms: ['docker'] },
  { canonical: 'kubernetes', synonyms: ['kubernetes', 'k8s'] },
  { canonical: 'terraform', synonyms: ['terraform'] },
  { canonical: 'jenkins', synonyms: ['jenkins'] },
  { canonical: 'cicd', synonyms: ['ci/cd', 'cicd', 'continuous integration', 'continuous deployment'] },
  { canonical: 'ansible', synonyms: ['ansible'] },
  { canonical: 'machine learning', synonyms: ['machine learning', 'ml'] },
  { canonical: 'deep learning', synonyms: ['deep learning'] },
  { canonical: 'tensorflow', synonyms: ['tensorflow'] },
  { canonical: 'pytorch', synonyms: ['pytorch'] },
  { canonical: 'nlp', synonyms: ['nlp', 'natural language processing'] },
  { canonical: 'data science', synonyms: ['data science'] },
  { canonical: 'pandas', synonyms: ['pandas'] },
  { canonical: 'numpy', synonyms: ['numpy'] },
  { canonical: 'selenium', synonyms: ['selenium'] },
  { canonical: 'junit', synonyms: ['junit'] },
  { canonical: 'jest', synonyms: ['jest'] },
  { canonical: 'cypress', synonyms: ['cypress'] },
  { canonical: 'rest api', synonyms: ['rest api', 'restful', 'rest'] },
  { canonical: 'graphql', synonyms: ['graphql'] },
  { canonical: 'microservices', synonyms: ['microservices', 'micro-services'] },
  { canonical: 'agile', synonyms: ['agile'] },
  { canonical: 'scrum', synonyms: ['scrum'] },
  { canonical: 'git', synonyms: ['git'] },
  { canonical: 'linux', synonyms: ['linux'] },
];

function buildSkillPattern(synonym: string): RegExp {
  const escaped = synonym.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  return new RegExp(`(?<![a-z0-9])${escaped}(?![a-z0-9])`, 'i');
}

const SKILL_DICTIONARY: SkillDictionaryEntry[] = RAW_SKILLS.map(({ canonical, synonyms }) => ({
  canonical,
  patterns: synonyms.map(buildSkillPattern),
}));

const DOMAIN_TERMS = [
  'banking', 'finance', 'financial services', 'healthcare', 'insurance', 'retail', 'e-commerce', 'ecommerce',
  'government', 'public sector', 'telecom', 'telecommunications', 'manufacturing', 'logistics', 'supply chain',
  'education', 'energy', 'utilities', 'pharma', 'pharmaceutical', 'biotech', 'gaming', 'saas', 'fintech',
  'edtech', 'hospitality', 'real estate', 'automotive', 'aerospace', 'cybersecurity', 'staffing', 'consulting',
];

const EDUCATION_TERMS = [
  'bachelor', "bachelor's", 'master', "master's", 'mba', 'phd', 'doctorate', 'b.s.', 'm.s.', 'b.tech',
  'associate degree', 'computer science', 'information technology', 'certified', 'certification', 'pmp',
  'cissp', 'cpa', 'comptia', 'scrum master certified', 'aws certified', 'azure certified',
];

const SCORE_BANDS: { min: number; label: string; color: string }[] = [
  { min: 80, label: 'Strong match', color: '#16a34a' },
  { min: 65, label: 'Good match', color: '#2563eb' },
  { min: 50, label: 'Possible match', color: '#d97706' },
  { min: 0, label: 'Weak match', color: '#dc2626' },
];

const REQUIRED_SECTION_HEADERS = ['required skills', 'requirements', 'required qualifications', 'must have', 'minimum qualifications', 'mandatory skills'];
const PREFERRED_SECTION_HEADERS = ['preferred skills', 'preferred qualifications', 'nice to have', 'desired skills', 'bonus', 'plus'];
const MANDATORY_LANGUAGE = ['must have', 'mandatory', 'must be', 'required', 'need to have', 'essential', 'non-negotiable'];

function getRecommendation(score: number): { label: string; color: string } {
  const band = SCORE_BANDS.find(b => score >= b.min) || SCORE_BANDS[SCORE_BANDS.length - 1];
  return { label: band.label, color: band.color };
}

function findSkillsInText(text: string): Set<string> {
  const found = new Set<string>();
  for (const entry of SKILL_DICTIONARY) {
    if (entry.patterns.some(p => p.test(text))) found.add(entry.canonical);
  }
  return found;
}

function skillMatchesText(skill: string, text: string, tags: string[]): boolean {
  const entry = SKILL_DICTIONARY.find(e => e.canonical === skill);
  if (entry) {
    if (entry.patterns.some(p => p.test(text))) return true;
  } else if (buildSkillPattern(skill).test(text)) {
    return true;
  }
  return tags.some(t => normalizeSkill(t) === skill);
}

function normalizeSkill(raw: string): string {
  const s = (raw || '').trim().toLowerCase();
  if (!s) return s;
  const entry = SKILL_DICTIONARY.find(e => e.canonical === s || e.patterns.some(p => p.test(s)));
  return entry ? entry.canonical : s;
}

function extractSection(text: string, headers: string[]): string | null {
  for (const h of headers) {
    const idx = text.indexOf(h);
    if (idx === -1) continue;
    const rest = text.slice(idx + h.length, idx + h.length + 800);
    const stop = rest.search(/\n\s*\n|\n[a-z][a-z /]{2,40}:\s*\n/);
    return stop > 0 ? rest.slice(0, stop) : rest;
  }
  return null;
}

function isMarkedMandatory(text: string, skill: string): boolean {
  const sentences = text.split(/[.\n]/);
  return sentences.some(s => skillMatchesText(skill, s, []) && MANDATORY_LANGUAGE.some(t => s.includes(t)));
}

function extractYearsRequirement(text: string): { min: number | null; max: number | null } {
  let m = text.match(/(\d{1,2})\s*-\s*(\d{1,2})\s*\+?\s*years?/);
  if (m) return { min: parseInt(m[1], 10), max: parseInt(m[2], 10) };
  m = text.match(/(?:minimum|min\.?|at least)\s*(?:of\s*)?(\d{1,2})\s*\+?\s*years?/);
  if (m) return { min: parseInt(m[1], 10), max: null };
  m = text.match(/(\d{1,2})\s*\+\s*years?/);
  if (m) return { min: parseInt(m[1], 10), max: null };
  m = text.match(/(\d{1,2})\s*years?\s*(?:of\s*)?experience/);
  if (m) return { min: parseInt(m[1], 10), max: null };
  return { min: null, max: null };
}

function extractMaxYears(text: string): number {
  const matches = text.matchAll(/(\d{1,2})\s*\+?\s*years?/g);
  let max = 0;
  for (const m of matches) max = Math.max(max, parseInt(m[1], 10));
  return max;
}

@Component({
  selector: 'app-jd-analyzer',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule, HttpClientModule],
  templateUrl: './jd-analyzer.html',
  styleUrls: ['./jd-analyzer.scss']
})
export class JdAnalyzer {
  jdText = '';
  submitting = false;
  message = '';
  selectedFileName = '';
  resultMatches: any[] = [];
  private readonly topN = 7;

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  constructor(private http: HttpClient) {}

  openDownload(id: any): void {
    if (!id) return;
    const url = `${environment.apiBaseUrl}/resumes/${id}/download`;
    try { window.open(url, '_blank'); } catch (e) { const a = document.createElement('a'); a.href = url; a.target = '_blank'; a.click(); }
  }

  triggerFilePicker(): void {
    try { this.fileInputRef?.nativeElement?.click(); } catch (e) {
      const el = document.getElementById('jd-file-input') as HTMLInputElement | null;
      if (el) el.click();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    if (!input || !input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.selectedFileName = file.name;

    // If it's a text file, read locally and analyze client-side
    if (file.type.startsWith('text') || file.name.toLowerCase().endsWith('.txt')) {
      const reader = new FileReader();
      reader.onload = () => { this.jdText = (reader.result || '').toString(); };
      reader.readAsText(file);
      return;
    }

    // For binary files (pdf/docx), attempt server-side analysis endpoint
    this.submitting = true;
    this.message = 'Uploading JD for server-side analysis...';

    const fd = new FormData();
    fd.append('file', file);

    const url = `${environment.apiBaseUrl}/analyze-jd`;

    this.http.post(url, fd, { reportProgress: true, observe: 'events', withCredentials: true }).subscribe({
      next: ev => {
        if (ev.type === HttpEventType.UploadProgress && ev.total) {
          const pct = Math.round(100 * (ev.loaded / ev.total));
          this.message = `Uploading JD... ${pct}%`;
        } else if ((ev as any).type === HttpEventType.Response) {
          this.submitting = false;
          const body: any = (ev as any).body;
          if (body && Array.isArray(body.matches)) {
            // Server-side matches use their own shape; render with the modal's plain fallback view.
            this.resultMatches = body.matches;
            this.message = `Found ${this.resultMatches.length} matches from server.`;
            this.showMatchesModal(this.resultMatches);
          } else if (body && body.error) {
            this.message = 'Server analysis returned an error. Falling back to client analysis.';
            if (body.extractedText) { this.jdText = body.extractedText; this.analyzeFromText(); }
          } else {
            this.message = 'No matches returned from server. Falling back to client analysis.';
          }
        }
      },
      error: err => {
        this.submitting = false;
        console.warn('Server analyze-jd failed', err);
        const errMsg = err?.error?.message || err?.message || JSON.stringify(err);
        this.message = 'Server analysis failed. You can paste JD text to analyze locally.';
        Swal.fire('Server analysis failed', `<pre style="text-align:left;white-space:pre-wrap">${errMsg}</pre>`, 'error');
      }
    });
  }

  analyzeFromText(): void {
    const t = (this.jdText || '').trim();
    if (!t) { this.message = 'Please provide job description text to analyze.'; return; }
    this.submitting = true;
    this.message = 'Analyzing resumes locally...';

    this.analyzeAndShortlist({ title: 'Ad-hoc JD', description: t, technologyStack: '' } as any).then(matches => {
      this.submitting = false;
      this.resultMatches = matches || [];
      this.message = `Found ${this.resultMatches.length} candidates ranked by fit.`;
      this.showMatchesModal(this.resultMatches);
    }).catch(err => {
      this.submitting = false;
      this.message = 'Analysis failed. See console for details.';
      console.error('Local analysis failed', err);
      Swal.fire('Local analysis failed', err?.message || String(err), 'error');
    });
  }

  private async analyzeAndShortlist(job: any): Promise<CandidateResult[]> {
    const jd = this.parseJobDescription(job);
    try {
      const api = `${environment.apiBaseUrl}/resumes`;
      const res: any = await this.http.get(api, { withCredentials: true }).toPromise();
      const resumes: any[] = Array.isArray(res?.content) ? res.content : (Array.isArray(res) ? res : []);
      return await this.scoreAndRankResumes(resumes, jd);
    } catch (err) {
      const em = (err as any)?.message || (err && JSON.stringify(err)) || 'Unknown error';
      const choice = await Swal.fire({
        title: 'Failed to load resumes',
        html: `Could not fetch resumes from server:<pre style="text-align:left;white-space:pre-wrap">${em}</pre><p>You can continue analysis using a small sample dataset (no server required).</p>`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Use sample resumes',
        cancelButtonText: 'Cancel'
      });

      if (choice.isConfirmed) {
        const sampleResumes = [
          { id: 's1', name: 'Alice Johnson', title: 'Frontend Developer', summary: '5 years React and Angular experience', tags: ['react', 'angular', 'typescript'] },
          { id: 's2', name: 'Bob Lee', title: 'Backend Engineer', summary: 'Node.js and Java expertise', tags: ['node', 'java', 'sql'] },
          { id: 's3', name: 'Carol Smith', title: 'Fullstack', summary: 'Fullstack with Python and React', tags: ['python', 'react', 'django'] }
        ];
        return await this.scoreAndRankResumes(sampleResumes, jd);
      }

      Swal.fire('Shortlist analysis error', 'Analysis aborted because resumes could not be loaded.', 'error');
      throw err;
    }
  }

  private async scoreAndRankResumes(resumes: any[], jd: JdAnalysis): Promise<CandidateResult[]> {
    const unique = this.dedupeResumes(resumes);
    const withText = await Promise.all(unique.map(async (r: any) => {
      const extracted = await this.fetchResumeText(r).catch(() => null);
      const text = (extracted || this.buildBaselineText(r)).toString();
      return { resume: r, text };
    }));

    const scored = withText.map(({ resume, text }) => this.scoreResume(resume, text, jd));
    scored.sort((a, b) => b.score - a.score);
    return scored.slice(0, this.topN);
  }

  private dedupeResumes(resumes: any[]): any[] {
    const seen = new Set<string>();
    const unique: any[] = [];
    for (const r of resumes || []) {
      const key = r?.id != null ? `id:${r.id}` : `ne:${(r?.name || '').toLowerCase()}|${(r?.email || '').toLowerCase()}`;
      if (seen.has(key)) continue;
      seen.add(key);
      unique.push(r);
    }
    return unique;
  }

  private buildBaselineText(r: any): string {
    return `${r.name || ''} ${r.summary || ''} ${(r.tags || []).join(' ')} ${r.title || ''} ${r.technologyStack || ''}`.trim();
  }

  private parseJobDescription(job: any): JdAnalysis {
    const text = `${job.title || ''}\n${job.description || ''}`.toLowerCase();
    const structuredSkills = (job.technologyStack || '').split(/[,;|]/).map((s: string) => s.trim()).filter(Boolean).map(normalizeSkill);

    const requiredSection = extractSection(text, REQUIRED_SECTION_HEADERS);
    const preferredSection = extractSection(text, PREFERRED_SECTION_HEADERS);

    const requiredFromSection = requiredSection ? findSkillsInText(requiredSection) : new Set<string>();
    const preferredFromSection = preferredSection ? findSkillsInText(preferredSection) : new Set<string>();
    const allSkillsInJd = findSkillsInText(text);

    let requiredPool: Set<string>;
    if (requiredFromSection.size > 0 || structuredSkills.length > 0) {
      requiredPool = new Set<string>([...structuredSkills, ...requiredFromSection]);
    } else {
      requiredPool = new Set<string>([...allSkillsInJd].filter(s => !preferredFromSection.has(s)));
    }

    const mandatorySet = new Set<string>(structuredSkills);
    for (const skill of requiredPool) {
      if (isMarkedMandatory(text, skill)) mandatorySet.add(skill);
    }

    const requiredSkills: RequiredSkill[] = Array.from(requiredPool).map(skill => ({ skill, mandatory: mandatorySet.has(skill) }));
    const preferredSkills = Array.from(preferredFromSection).filter(s => !requiredPool.has(s));

    const years = extractYearsRequirement(text);
    const titleTokens = this.tokenize((job.title || '').toLowerCase());
    const domainTerms = DOMAIN_TERMS.filter(term => text.includes(term));
    const educationTerms = EDUCATION_TERMS.filter(term => text.includes(term));
    const noSponsorship = /(no|without|not\s+provide|unable to provide|cannot provide)\s+(visa\s+)?sponsorship|not\s+sponsor|us citizen only|green card required/i.test(text);

    return { requiredSkills, preferredSkills, minYears: years.min, maxYears: years.max, titleTokens, domainTerms, educationTerms, noSponsorship };
  }

  private scoreResume(resume: any, text: string, jd: JdAnalysis): CandidateResult {
    const lowerText = (text || '').toLowerCase();
    const tags = Array.isArray(resume.tags) ? resume.tags : [];

    // Required skills (40%) — partial matching, weighted toward mandatory ones, penalized if any mandatory skill is missing.
    const matchedRequired: string[] = [];
    const missingRequired: string[] = [];
    const missingMandatory: string[] = [];
    let weightedMatched = 0;
    let weightedTotal = 0;
    for (const { skill, mandatory } of jd.requiredSkills) {
      const weight = mandatory ? 1.5 : 1;
      weightedTotal += weight;
      if (skillMatchesText(skill, lowerText, tags)) {
        matchedRequired.push(skill);
        weightedMatched += weight;
      } else {
        missingRequired.push(skill);
        if (mandatory) missingMandatory.push(skill);
      }
    }
    let requiredScore = weightedTotal > 0 ? (weightedMatched / weightedTotal) * 100 : 70;
    if (missingMandatory.length > 0) requiredScore *= 0.7;
    requiredScore = Math.max(0, Math.min(100, requiredScore));

    // Preferred skills (10%)
    const matchedPreferred = jd.preferredSkills.filter(s => skillMatchesText(s, lowerText, tags));
    const preferredScore = jd.preferredSkills.length > 0 ? (matchedPreferred.length / jd.preferredSkills.length) * 100 : 70;

    // Experience and years (20%)
    const candidateYears = extractMaxYears(lowerText);
    const experienceScore = jd.minYears != null
      ? (candidateYears >= jd.minYears ? 100 : Math.max(0, (candidateYears / jd.minYears) * 100))
      : (candidateYears > 0 ? 75 : 55);

    // Job-title and role relevance (10%)
    const resumeTitleTokens = this.tokenize((resume.title || '').toLowerCase());
    let titleScore = 60;
    if (jd.titleTokens.length > 0) {
      const overlap = jd.titleTokens.filter(t => resumeTitleTokens.includes(t));
      const union = new Set([...jd.titleTokens, ...resumeTitleTokens]);
      titleScore = union.size > 0 ? (overlap.length / union.size) * 100 : 60;
    }

    // Domain / industry experience (10%)
    let domainScore = 70;
    if (jd.domainTerms.length > 0) {
      const matchedDomain = jd.domainTerms.filter(t => lowerText.includes(t));
      domainScore = (matchedDomain.length / jd.domainTerms.length) * 100;
    }

    // Education and certifications (5%)
    let educationScore = 80;
    if (jd.educationTerms.length > 0) {
      const matchedEdu = jd.educationTerms.filter(t => lowerText.includes(t));
      educationScore = (matchedEdu.length / jd.educationTerms.length) * 100;
    }

    // Location, availability, and work authorization (5%)
    let locationScore = 75;
    if (jd.noSponsorship) {
      const visa = (resume.visaStatus || '').toLowerCase();
      const authorizedNoSponsor = /(us citizen|citizen|green card|permanent resident|\bgc\b)/.test(visa);
      const needsSponsorship = /(h1b|h-1b|opt|cpt|f1|f-1)/.test(visa);
      locationScore = authorizedNoSponsor ? 100 : needsSponsorship ? 20 : 60;
    }

    const breakdown: ScoreBreakdown = {
      requiredSkills: Math.round(requiredScore),
      experience: Math.round(experienceScore),
      titleRelevance: Math.round(titleScore),
      preferredSkills: Math.round(preferredScore),
      domain: Math.round(domainScore),
      education: Math.round(educationScore),
      location: Math.round(locationScore),
    };

    const score = Math.round(
      breakdown.requiredSkills * 0.40 +
      breakdown.experience * 0.20 +
      breakdown.titleRelevance * 0.10 +
      breakdown.preferredSkills * 0.10 +
      breakdown.domain * 0.10 +
      breakdown.education * 0.05 +
      breakdown.location * 0.05
    );

    const { label: recommendation } = getRecommendation(score);
    const { strengths, concerns } = this.buildStrengthsAndConcerns(breakdown, jd, missingMandatory, missingRequired, candidateYears);

    return {
      id: resume.id,
      name: resume.name,
      title: resume.title,
      score,
      recommendation,
      breakdown,
      matchedRequiredSkills: matchedRequired,
      missingRequiredSkills: missingRequired,
      missingMandatorySkills: missingMandatory,
      matchedPreferredSkills: matchedPreferred,
      experienceDetail: { requiredYears: jd.minYears, candidateYears },
      strengths,
      concerns,
      _extractedText: text,
    };
  }

  private buildStrengthsAndConcerns(
    breakdown: ScoreBreakdown,
    jd: JdAnalysis,
    missingMandatory: string[],
    missingRequired: string[],
    candidateYears: number
  ): { strengths: string[]; concerns: string[] } {
    const strengths: string[] = [];
    const concerns: string[] = [];

    if (breakdown.requiredSkills >= 80) strengths.push('Strong match on required skills');
    if (missingMandatory.length) concerns.push(`Missing mandatory skill(s): ${missingMandatory.join(', ')}`);
    else if (missingRequired.length) concerns.push(`Missing some required skills: ${missingRequired.slice(0, 5).join(', ')}`);

    if (breakdown.experience >= 80) strengths.push('Meets or exceeds required experience');
    else if (breakdown.experience < 50 && jd.minYears) concerns.push(`Experience may fall short (requires ~${jd.minYears}+ yrs, found ~${candidateYears})`);

    if (breakdown.titleRelevance >= 70) strengths.push('Title/role closely matches the job');
    if (breakdown.preferredSkills >= 70 && jd.preferredSkills.length) strengths.push('Covers several preferred skills');

    if (jd.domainTerms.length) {
      if (breakdown.domain >= 70) strengths.push('Relevant domain/industry background');
      else if (breakdown.domain < 40) concerns.push('Limited evidence of relevant industry experience');
    }

    if (jd.noSponsorship && breakdown.location < 50) concerns.push('Work authorization may not meet role requirements (no sponsorship)');

    return { strengths, concerns };
  }

  private showMatchesModal(matches: CandidateResult[] | any[]): void {
    const html = matches.map((s: any) => {
      const downloadBtn = s.id ? `<a href="${environment.apiBaseUrl}/resumes/${s.id}/download" target="_blank" class="swal-download" style="display:inline-block;margin-top:6px">Download</a>` : '';
      const plainText = (s._extractedText || s.plainText || '').toString();
      const txtBlock = plainText ? `<details style="margin-top:8px"><summary style="cursor:pointer;color:#2563eb">View plain text</summary><pre style="white-space:pre-wrap;max-height:260px;overflow:auto;padding:8px;background:#f8fafc;border-radius:4px;margin-top:6px">${this.escapeHtml(plainText)}</pre></details>` : '';

      if (!s.breakdown) {
        // Fallback for shapes without a full breakdown (e.g. server-side /analyze-jd matches).
        const miss = s.missingSkills && s.missingSkills.length ? `<div style="color:#d9534f;font-weight:600">Missing: ${s.missingSkills.join(', ')}</div>` : '<div style="color:#28a745">No major skills missing</div>';
        return `
          <div style="padding:10px;border-bottom:1px solid #eef2f6">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <div>
                <div style="font-weight:700">${s.name || 'N/A'} <span style="color:#6b7280;font-weight:500">(${s.title || '—'})</span></div>
                <div style="font-size:0.9rem;color:#6b7280">Score: ${s.score} • Matched: ${(s.matched || []).join(', ') || '—'}</div>
              </div>
              <div style="text-align:right">${downloadBtn}</div>
            </div>
            <div style="margin-top:8px">${miss}</div>
            ${txtBlock}
          </div>
        `;
      }

      const rec = getRecommendation(s.score);
      const strengthsHtml = s.strengths.length ? `<div style="margin-top:6px"><strong>Strengths:</strong><ul style="margin:4px 0 0 18px;padding:0">${s.strengths.map((x: string) => `<li>${this.escapeHtml(x)}</li>`).join('')}</ul></div>` : '';
      const concernsHtml = s.concerns.length ? `<div style="margin-top:6px;color:#b45309"><strong>Concerns:</strong><ul style="margin:4px 0 0 18px;padding:0">${s.concerns.map((x: string) => `<li>${this.escapeHtml(x)}</li>`).join('')}</ul></div>` : '';
      const reqHtml = `<div style="margin-top:6px"><strong>Required skills:</strong> matched ${s.matchedRequiredSkills.join(', ') || '—'}${s.missingMandatorySkills.length ? `; <span style="color:#dc2626">missing mandatory: ${s.missingMandatorySkills.join(', ')}</span>` : ''}${(!s.missingMandatorySkills.length && s.missingRequiredSkills.length) ? `; missing: ${s.missingRequiredSkills.join(', ')}` : ''}</div>`;
      const prefHtml = s.matchedPreferredSkills.length ? `<div style="margin-top:4px"><strong>Preferred skills matched:</strong> ${s.matchedPreferredSkills.join(', ')}</div>` : '';
      const expHtml = `<div style="margin-top:4px"><strong>Experience:</strong> ${s.experienceDetail.requiredYears ? `${s.experienceDetail.candidateYears} yrs found vs ${s.experienceDetail.requiredYears}+ required` : `${s.experienceDetail.candidateYears} yrs found (no explicit requirement detected)`}</div>`;
      const breakdownHtml = `<div style="margin-top:8px;font-size:0.85rem;color:#6b7280">Breakdown — Required: ${s.breakdown.requiredSkills} • Experience: ${s.breakdown.experience} • Title: ${s.breakdown.titleRelevance} • Preferred: ${s.breakdown.preferredSkills} • Domain: ${s.breakdown.domain} • Education: ${s.breakdown.education} • Location: ${s.breakdown.location}</div>`;

      return `
        <div style="padding:12px;border-bottom:1px solid #eef2f6">
          <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div>
              <div style="font-weight:700">${s.name || 'N/A'} <span style="color:#6b7280;font-weight:500">(${s.title || '—'})</span></div>
              <div style="margin-top:2px"><span style="background:${rec.color};color:#fff;padding:2px 10px;border-radius:12px;font-size:0.8rem;font-weight:600">${rec.label}</span> <span style="margin-left:8px;font-weight:700">${s.score}/100</span></div>
            </div>
            <div style="text-align:right">${downloadBtn}</div>
          </div>
          ${reqHtml}
          ${prefHtml}
          ${expHtml}
          ${strengthsHtml}
          ${concernsHtml}
          ${breakdownHtml}
          ${txtBlock}
        </div>
      `;
    }).join('');

    Swal.fire({
      title: 'Ranked Candidates',
      html: `<div style="text-align:left"><div style="max-height:460px;overflow:auto">${html}</div></div>`,
      width: 900,
      confirmButtonText: 'Ok'
    });
  }

  private tokenize(text: string): string[] {
    return text.split(/[^a-z0-9]+/i).map(t => t.trim()).filter(Boolean).filter((w: string) => w.length > 1);
  }

  // Attempt to extract full text from a resume record.
  // Strategy: use provided fields, else try a text endpoint `/resumes/{id}/text`, else try download and treat as fallback.
  private async fetchResumeText(resume: any): Promise<string | null> {
    if (!resume) return null;
    if (resume.fullText && typeof resume.fullText === 'string' && resume.fullText.trim()) return resume.fullText;
    if (resume.parsedText && typeof resume.parsedText === 'string' && resume.parsedText.trim()) return resume.parsedText;

    const baseline = this.buildBaselineText(resume);
    if (resume.id) {
      try {
        const textUrl = `${environment.apiBaseUrl}/resumes/${resume.id}/text`;
        const res: any = await this.http.get(textUrl, { responseType: 'text' as 'json', withCredentials: true }).toPromise();
        if (res && typeof res === 'string' && res.trim()) return res;
      } catch (e) {
        console.debug('resume text endpoint failed for', resume.id, e);
      }

      try {
        const dlUrl = `${environment.apiBaseUrl}/resumes/${resume.id}/download`;
        const res2: any = await this.http.get(dlUrl, { responseType: 'text' as 'json', withCredentials: true }).toPromise();
        if (res2 && typeof res2 === 'string' && res2.trim()) return res2;
      } catch (e) {
        console.debug('resume download as text failed for', resume.id, e);
      }
    }

    return baseline || null;
  }

  // Simple HTML escape for safe insertion into modal
  private escapeHtml(s: string): string {
    if (!s) return '';
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
}
