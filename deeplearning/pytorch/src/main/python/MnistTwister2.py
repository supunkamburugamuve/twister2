#  // Licensed under the Apache License, Version 2.0 (the "License");
#  // you may not use this file except in compliance with the License.
#  // You may obtain a copy of the License at
#  //
#  // http://www.apache.org/licenses/LICENSE-2.0
#  //
#  // Unless required by applicable law or agreed to in writing, software
#  // distributed under the License is distributed on an "AS IS" BASIS,
#  // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  // See the License for the specific language governing permissions and
#  // limitations under the License.

from twister2 import TSetContext
from twister2.Twister2Environment import Twister2Environment
from twister2.tset.fn.SourceFunc import SourceFunc

env = Twister2Environment(resources=[{"cpu": 1, "ram": 512, "instances": 4}])

print("Hello from worker %d" % env.worker_id)


class IntSource(SourceFunc):

    def __init__(self):
        super().__init__()
        self.i = 0

    def has_next(self):
        return self.i < 10

    def next(self):
        res = self.i
        self.i = self.i + 1
        return res


source = env.create_source(IntSource(), 4)


def mul_by_five(itr, collector, ctx: TSetContext):
    for i in itr:
        collector.collect(i * 5)


def add_two(itr, collector, ctx: TSetContext):
    for i in itr:
        collector.collect(i + 2)


source.compute(mul_by_five).compute(add_two).for_each(lambda i: print("(i x 5) + 2 = %d" % i))
