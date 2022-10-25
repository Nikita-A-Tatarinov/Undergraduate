import argparse
import os
import re


def initialize_parser():
    parser = argparse.ArgumentParser(description='Removes new lines')
    parser.add_argument('--datapath', default='prelecture.txt', help='Path to text data')
    parser.add_argument('--outputdir', default='./', help='Relative directory for output files')
    parser.add_argument('--outputfilename', default='lecture_no_newlines.txt', help='Name of output file')
    args = parser.parse_args()
    return parser, args


if __name__ == '__main__':
    parser, args = initialize_parser()
    os.makedirs(args.outputdir, exist_ok=True)
    with open(args.datapath) as f:
        data = f.read()
    with open(os.path.join(args.outputdir, args.outputfilename), 'w') as f:
        f.write(re.sub(' +', ' ', data.replace('\n', ' ')))
