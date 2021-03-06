#!/usr/bin/env python

# Copyright 2011-2012 GRNET S.A. All rights reserved.
#
# Redistribution and use in source and binary forms, with or
# without modification, are permitted provided that the following
# conditions are met:
#
#   1. Redistributions of source code must retain the above
#      copyright notice, this list of conditions and the following
#      disclaimer.
#
#   2. Redistributions in binary form must reproduce the above
#      copyright notice, this list of conditions and the following
#      disclaimer in the documentation and/or other materials
#      provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
# OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
# USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
# AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#
# The views and conclusions contained in the software and
# documentation are those of the authors and should not be
# interpreted as representing official policies, either expressed
# or implied, of GRNET S.A.

import distribute_setup
distribute_setup.use_setuptools()

import os
import sys

from fnmatch import fnmatchcase
from distutils.util import convert_path

from setuptools import setup, find_packages


HERE = os.path.abspath(os.path.normpath(os.path.dirname(__file__)))

from pithos_webclient.version import __version__

# Package info
VERSION = __version__
README = open(os.path.join(HERE, 'README')).read()
CHANGES = open(os.path.join(HERE, 'Changelog')).read()
SHORT_DESCRIPTION = 'Package short description'

PACKAGES_ROOT = '.'
PACKAGES = find_packages(PACKAGES_ROOT)

# Package meta
CLASSIFIERS = [
    'Development Status :: 3 - Alpha',
    'Operating System :: OS Independent',
    'Programming Language :: Python',
    'Topic :: Utilities',
    'License :: OSI Approved :: BSD License',
]

# Package requirements
INSTALL_REQUIRES = [
    'Django>=1.2, <1.5',
    'snf-branding',
    'snf-common>=0.9.0rc'
]

EXTRAS_REQUIRES = {
}

TESTS_REQUIRES = [
]

# Provided as an attribute, so you can append to these instead
# of replicating them:
standard_exclude = ["*.py", "*.pyc", "*$py.class", "*~", ".*", "*.bak"]
standard_exclude_directories = [
    ".*", "CVS", "_darcs", "./build", "./dist", "EGG-INFO", "*.egg-info",
    "snf-0.7"
]


# (c) 2005 Ian Bicking and contributors; written for Paste
# (http://pythonpaste.org) Licensed under the MIT license:
# http://www.opensource.org/licenses/mit-license.php Note: you may want to
# copy this into your setup.py file verbatim, as you can't import this from
# another package, when you don't know if that package is installed yet.
def find_package_data(
    where=".",
    package="",
    exclude=standard_exclude,
    exclude_directories=standard_exclude_directories,
    only_in_packages=True,
    show_ignored=False
):
    """
    Return a dictionary suitable for use in ``package_data``
    in a distutils ``setup.py`` file.

    The dictionary looks like::

        {"package": [files]}

    Where ``files`` is a list of all the files in that package that
    don"t match anything in ``exclude``.

    If ``only_in_packages`` is true, then top-level directories that
    are not packages won"t be included (but directories under packages
    will).

    Directories matching any pattern in ``exclude_directories`` will
    be ignored; by default directories with leading ``.``, ``CVS``,
    and ``_darcs`` will be ignored.

    If ``show_ignored`` is true, then all the files that aren"t
    included in package data are shown on stderr (for debugging
    purposes).

    Note patterns use wildcards, or can be exact paths (including
    leading ``./``), and all searching is case-insensitive.
    """
    out = {}
    stack = [(convert_path(where), "", package, only_in_packages)]
    while stack:
        where, prefix, package, only_in_packages = stack.pop(0)
        for name in os.listdir(where):
            fn = os.path.join(where, name)
            if os.path.isdir(fn):
                bad_name = False
                for pattern in exclude_directories:
                    if (fnmatchcase(name, pattern)
                            or fn.lower() == pattern.lower()):
                        bad_name = True
                        if show_ignored:
                            print >> sys.stderr, (
                                "Directory %s ignored by pattern %s"
                                % (fn, pattern))
                        break
                if bad_name:
                    continue
                if (os.path.isfile(os.path.join(fn, "__init__.py"))
                        and not prefix):
                    if not package:
                        new_package = name
                    else:
                        new_package = package + "." + name
                    stack.append((fn, "", new_package, False))
                else:
                    stack.append((fn, prefix + name + "/", package,
                                  only_in_packages))
            elif package or not only_in_packages:
                # is a file
                bad_name = False
                for pattern in exclude:
                    if (fnmatchcase(name, pattern) or
                            fn.lower() == pattern.lower()):
                        bad_name = True
                        if show_ignored:
                            print >> sys.stderr, (
                                "File %s ignored by pattern %s"
                                % (fn, pattern))
                        break
                if bad_name:
                    continue
                out.setdefault(package, []).append(prefix + name)
    return out


"""
Gwt clea/build helpers
"""
import subprocess as sp
import glob


def clean_gwt(root="../", public_dir="bin/www/gr.grnet.pithos.web.Pithos/"):
    # skip if no build.xml found (debian build process)
    if not os.path.exists(os.path.join(root, "build.xml")):
        return

    curdir = os.getcwd()
    os.chdir(root)
    rcode = sp.call(["ant", "clean"])
    if rcode == 1:
        raise Exception("GWT clean failed")
    os.chdir(curdir)
    #pub_dir = os.path.abspath(os.path.join(root, public_dir))
    static_dir = os.path.abspath(os.path.join("pithos_webclient", "static",
                                              "pithos_webclient"))
    #templates_dir = os.path.abspath(os.path.join("pithos_webclient",
                                                 #"templates",
                                                 #"pithos_webclient"))
    clean_static = ["rm", "-r"] + glob.glob(os.path.join(static_dir, "*"))

    # clean dirs
    if len(clean_static) > 2:
        sp.call(clean_static)


def build_gwt(root="../", public_dir="bin/www/gr.grnet.pithos.web.Pithos/"):
    # skip if no build.xml found (debian build process)
    if not os.path.exists(os.path.join(root, "build.xml")):
        return

    curdir = os.getcwd()
    os.chdir(root)
    # run ant on root dir
    rcode = sp.call(["ant"])
    if rcode == 1:
        raise Exception("GWT build failed")
    os.chdir(curdir)

    pub_dir = os.path.abspath(os.path.join(root, public_dir))
    static_dir = os.path.abspath(os.path.join("pithos_webclient", "static",
                                              "pithos_webclient"))
    templates_dir = os.path.abspath(os.path.join("pithos_webclient",
                                                 "templates",
                                                 "pithos_webclient"))

    clean_static = ["rm", "-r"] + glob.glob(os.path.join(static_dir, "*"))

    # clean dirs
    if len(clean_static) > 2:
        sp.call(clean_static)

    copy_static = ["cp", "-r"] + glob.glob(os.path.join(pub_dir, "*")) + \
                  [static_dir]
    copy_index = ["cp", os.path.join(pub_dir, "index.html"), templates_dir]
    sp.call(copy_static)
    sp.call(copy_index)

    index = os.path.join(templates_dir, "index.html")
    index_data = file(index).read()
    # fix locations of static files
    index_data = index_data.replace('href="',
                                    'href="{{ MEDIA_URL }}pithos_webclient/')
    index_data = index_data.replace('" src="',
                                    '" src="{{ MEDIA_URL }}pithos_webclient/')
    index_data = index_data.replace(
        '\' src=\'',
        '\' src=\'{{ MEDIA_URL }}pithos_webclient/')
    index_data = index_data.replace('url(',
                                    'url({{ MEDIA_URL }}pithos_webclient/')

    ifile = file(index, "w+")
    ifile.write(index_data)
    ifile.close()


# do we need to run ant ???
if any(x in ''.join(sys.argv) for x in ["sdist", "build", "develop",
                                        "install"]):
    build_gwt()

if any(x in ''.join(sys.argv) for x in ["clean"]):
    clean_gwt()


setup(
    name='snf-pithos-webclient',
    version=VERSION,
    license='BSD',
    url='http://code.grnet.gr/projects/pithos-web-client',
    description=SHORT_DESCRIPTION,
    long_description=README + '\n\n' + CHANGES,
    classifiers=CLASSIFIERS,
    author='GRNET',
    author_email='pithos@grnet.gr',

    packages=find_packages(),
    include_package_data=True,
    package_data=find_package_data('.'),
    zip_safe=False,

    install_requires=INSTALL_REQUIRES,

    dependency_links=['http://docs.dev.grnet.gr/pypi'],

    entry_points={
        'synnefo': [
            'web_apps = pithos_webclient.synnefo_settings:installed_apps',
            'urls = pithos_webclient.urls:urlpatterns',
            'web_static = pithos_webclient.synnefo_settings:static_files',
            'web_context_processors = '
            'pithos_webclient.synnefo_settings:context_processors'
        ]
    }
)
