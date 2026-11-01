#!/usr/bin/env python3
"""Merge upstream (TF 4.8, 1.21.1) Crowdin translations into this 1.20.1 fork.

Only keys that exist in the fork's generated en_us.json are merged, so lang
files never advertise content the fork does not have. Re-run it after a
backported feature adds its keys to en_us.json to pull in their translations.

Rules, per language file shipped upstream:
  * The fork key set and English come from
    src/generated/resources/assets/twilightforest/lang/en_us.json.
  * A fork key that upstream renamed is mapped to the upstream key whose English
    text is identical (e.g. item.twilightforest.music_disc_X.desc ->
    jukebox_song.twilightforest.X, renamed subtitle keys).
  * An upstream translation is taken when the English source text is unchanged
    between the fork and upstream, it is really translated (differs from the
    upstream English) and it references the same %s / %n$s arguments.
  * When the English text changed upstream, the translation only fills a key the
    fork has not translated yet, and never for keys whose meaning changed
    (SEMANTIC_CHANGES); otherwise the fork's translation stays.
  * Keys the fork already translates but upstream dropped are kept.
  * en_us/en_ud are skipped (en_ud is generated from en_us), and so are files
    that are not strict JSON (the hand-written es_ar/es_cl/es_ec/es_mx with
    // comments, unchanged upstream).

usage: merge_upstream_lang.py <upstream-clone> [<git-ref>]   (default ref HEAD)
"""
import json
import os
import re
import subprocess
import sys

FORK = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FORK_LANG = os.path.join(FORK, 'src/main/resources/assets/twilightforest/lang')
FORK_EN = os.path.join(FORK, 'src/generated/resources/assets/twilightforest/lang/en_us.json')
UP_LANG = 'src/main/resources/assets/twilightforest/lang'
UP_EN = 'src/generated/resources/assets/twilightforest/lang/en_us.json'
SKIP = {'en_us.json', 'en_ud.json'}

# English changed upstream because the behaviour/content changed, so an
# upstream translation would describe something the fork does not do.
SEMANTIC_CHANGES = re.compile(r'^(?:'
	r'advancement\.twilightforest\.full_mettle_alchemist\.desc'
	r'|advancement\.twilightforest\.naga_armors\.desc'
	r'|block\.twilightforest\.knightmetal_block\.desc'
	r'|block\.minecraft\.banner\.twilightforest\.snow_queen\..*'
	r'|item\.twilightforest\.snow_queen_banner_pattern\.desc'
	r'|subtitles\.twilightforest\.entity\.knight_phantom\.(?:axe|pickaxe)'
	r'|subtitles\.twilightforest\.entity\.troll\.throw_rock'
	r'|subtitles\.twilightforest\.item\.block_and_chain\.fire'
	r')$')

ARG = re.compile(r'%(?:(\d+)\$)?([sd%])')


def args(text):
	"""Set of argument indices a Minecraft translation string consumes."""
	used, seq = set(), 0
	for m in ARG.finditer(text):
		if m.group(2) == '%':
			continue
		if m.group(1):
			used.add(int(m.group(1)))
		else:
			seq += 1
			used.add(seq)
	return used


def git_show(repo, ref, path):
	return subprocess.run(['git', '-C', repo, 'show', f'{ref}:{path}'], check=True, capture_output=True).stdout.decode('utf-8')


def dump(data, trailing_newline):
	text = json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False)
	return text + ('\n' if trailing_newline else '')


def main():
	if len(sys.argv) < 2:
		sys.exit(__doc__)
	repo, ref = sys.argv[1], (sys.argv[2] if len(sys.argv) > 2 else 'HEAD')
	with open(FORK_EN, encoding='utf-8') as f:
		en_fork = json.load(f)
	en_up = json.loads(git_show(repo, ref, UP_EN))

	# fork key -> upstream key (identity, or a rename with identical English)
	by_text = {}
	for k, v in en_up.items():
		if k not in en_fork:
			by_text.setdefault(v, []).append(k)
	key_map = {}
	for k, v in en_fork.items():
		if k in en_up:
			key_map[k] = k
		elif len(by_text.get(v, [])) == 1:
			key_map[k] = by_text[v][0]

	files = subprocess.run(['git', '-C', repo, 'ls-tree', '--name-only', ref, UP_LANG + '/'], check=True, capture_output=True).stdout.decode().split()
	for path in files:
		name = os.path.basename(path)
		if name in SKIP:
			continue
		raw_up = git_show(repo, ref, path)
		try:
			up = json.loads(raw_up)
		except json.JSONDecodeError:
			continue
		if not up:
			continue
		fork_path = os.path.join(FORK_LANG, name)
		fork = {}
		if os.path.exists(fork_path):
			with open(fork_path, encoding='utf-8') as f:
				raw_fork = f.read()
			try:
				fork = json.loads(raw_fork)
			except json.JSONDecodeError:
				continue
		merged = dict(fork)
		for k, en in en_fork.items():
			uk = key_map.get(k)
			if uk is None or uk not in up:
				continue
			tr = up[uk]
			if tr == en_up[uk] or args(tr) != args(en):
				continue  # untranslated upstream, or arguments do not fit the fork's string
			if en_up[uk] == en:
				merged[k] = tr
			elif k not in fork and not SEMANTIC_CHANGES.match(k):
				merged[k] = tr
		if merged == fork:
			continue
		out = dump(merged, raw_up.endswith('\n'))
		with open(fork_path, 'w', encoding='utf-8') as f:
			f.write(out)
		print(f'{name}: {len(fork)} -> {len(merged)} keys')


if __name__ == '__main__':
	main()
