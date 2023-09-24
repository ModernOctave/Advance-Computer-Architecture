from main import index


def log(value, end='\n'):
	with open(f'stats.{index}.log', 'a') as f:
		f.write(str(value) + end)